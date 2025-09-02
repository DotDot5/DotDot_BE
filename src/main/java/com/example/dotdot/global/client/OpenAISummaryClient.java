package com.example.dotdot.global.client;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.search.GoogleSearchErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAISummaryClient {

    @Value("${openai-summary.api-key}")
    private String apiKey;

    @Value("${openai-summary.base-url}")
    private String baseUrl;

    @Value("${openai-summary.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String summarize(String fullText) {
        try {
            String prompt = """
                    다음 회의록 내용을 5문장 이내로 요약해 주세요.

                    [회의 내용]
                    %s
                    """.formatted(fullText);

            String requestBody = objectMapper.writeValueAsString(new OpenAIRequest(model, prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("OpenAI 응답 실패: {}", response.getStatusCode());
                throw new AppException(GoogleSearchErrorCode.GPT_API_ERROR);
            }

            String body = response.getBody();
            log.info("요약 결과 응답: {}", body);

            return extractSummary(body);

        } catch (Exception e) {
            log.error("요약 중 예외 발생", e);
            throw new AppException(GoogleSearchErrorCode.GPT_API_ERROR);
        }
    }

    private String extractSummary(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        return root.at("/choices/0/message/content").asText();
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
