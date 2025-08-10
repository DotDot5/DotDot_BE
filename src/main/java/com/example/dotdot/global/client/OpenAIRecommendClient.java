package com.example.dotdot.global.client;

import com.example.dotdot.domain.Agenda;
import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.search.GoogleSearchErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAIRecommendClient {
    @Value("${openai-recommend.api-key}")
    private String apiKey;

    @Value("${openai-recommend.base-url}")
    private String baseUrl;

    @Value("${openai-recommend.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 여러 개의 회의 안건과 회의 요약을 받아 GPT에게 최적의 구글 검색 쿼리 3개를 생성 요청하는 메서드
     */
    public List<String> generateSearchQueries(List<Agenda> agendas,String meetingSummary){
        try {
            StringBuilder agendaBuilder = new StringBuilder("회의 안건 목록:\n");
            for (int i = 0; i < agendas.size(); i++) {
                Agenda agenda = agendas.get(i);
                agendaBuilder.append(i + 1).append(") 제목: ").append(agenda.getAgenda()).append("\n")
                        .append("   상세 내용: ").append(agenda.getBody()).append("\n\n");
            }
            String prompt = """
            아래는 한 회의의 주요 정보입니다.

            %s

            회의 요약: %s

            이 회의 정보를 참고하여, 구글에서 검색할 때  
            “관련성 높고 신뢰도 있는 자료를 찾을 수 있도록 최적화된 구체적인 검색 문장(키워드) 3개”를 추천해 주세요.

            검색 문장은 다음 조건을 반드시 만족해야 합니다:
            - 각 문장은 10~20단어 내외로 간결하게 작성할 것
            - 3개의 문장은 서로 겹치지 않는 독립적인 내용으로 할 것
            - 공식 문서, 논문, 정부/교육기관 사이트 등 신뢰할 수 있는 출처에서 유용한 결과가 나오도록 할 것
            - 가능하면 ‘site:edu OR site:gov OR site:korea.kr’ 같은 도메인 제한 키워드 포함 권장
            - 각 문장은 절대 번호, 글머리 기호, 점, 괄호, 따옴표 등 어떤 구분 기호도 포함하지 말고 순수한 텍스트만 한 줄씩 출력하세요.
            - 번호를 붙이거나 목록 형태로 출력하지 마십시오
            """.formatted(agendaBuilder.toString(), meetingSummary);

            String requestBody = objectMapper.writeValueAsString(new OpenAIRequest(model, prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("OpenAI API 비정상 응답 코드: {}", response.getStatusCode());
                throw new AppException(GoogleSearchErrorCode.GPT_API_ERROR);
            }

            String responseBody = response.getBody();
            log.info("[GPT 응답 원본] {}", responseBody);

            return parseQueriesFromResponse(responseBody);

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 예외 발생", e);
            throw new AppException(GoogleSearchErrorCode.GPT_API_ERROR);
        }
    }
    private List<String> parseQueriesFromResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        String text = root.at("/choices/0/message/content").asText();

        List<String> queries = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            String noNumber = line.replaceFirst("^\\d+\\.\\s*", "");
            String cleaned = noNumber.replaceFirst("^[-*•\\s]+", "");
            String trimmed = cleaned.trim();
            if (!trimmed.isEmpty()) {
                queries.add(trimmed);
            }
        }
        return queries;
    }

    static class OpenAIRequest {
        public String model;
        public Message[] messages;

        public OpenAIRequest(String model, String prompt) {
            this.model = model;
            this.messages = new Message[]{new Message("user", prompt)};
        }
    }

    static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

}
