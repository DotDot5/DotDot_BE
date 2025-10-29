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

import java.util.ArrayList;
import java.util.List;

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

    private static final int CHUNK_SIZE = 12000;
    private static final int CHUNK_OVERLAP = 500;

    // 프롬프트
    private String buildPrompt(String content, String agenda, String instruction) {
        return """
                %s

                [참고 안건]
                %s

                [회의 내용]
                %s
                """.formatted(instruction, (agenda == null || agenda.isBlank() ? "(안건 없음)" : agenda), content);
    }

    // 텍스트를 겹치는 조각으로 나눔
    private List<String> splitText(String text, int maxChunkSize) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(text.length(), start + maxChunkSize);
            chunks.add(text.substring(start, end));

            // 다음 시작 위치를 겹치도록 조정 (마지막 조각 제외)
            start = end - (end == text.length() ? 0 : CHUNK_OVERLAP);
        }
        return chunks;
    }

    // OpenAI API를 호출
    private String callOpenAI(String prompt) {
        try {
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

            return extractSummary(body);

        } catch (Exception e) {
            log.error("요약 중 예외 발생", e);
            throw new AppException(GoogleSearchErrorCode.GPT_API_ERROR.withDetail(e.getMessage()));
        }
    }

    // 요약 + 할 일이 포함된 텍스트에서 순수 요약 부분만 추출하는 AI
    public String extractCleanSummary(String fullSummaryWithTasks) {
        log.info("Extracting clean summary from full result...");

        String prompt = """
            다음 [입력 텍스트]에서 '핵심 요약' 부분(10문장 이내)만 정확하게 추출해서, 다른 부가 설명이나 '할 일' 목록 없이 순수한 요약 내용만 한글로 출력해줘.

            [입력 텍스트]
            %s
            """.formatted(fullSummaryWithTasks);

        return callOpenAI(prompt);
    }

    public String summarize(String transcript, String agendaText) {
        int agendaLength = (agendaText != null ? agendaText.length() : 0);
        int effectiveChunkSize = CHUNK_SIZE - agendaLength - 1000;

        // Transcript가 한도보다 짧으면, 기존 방식대로 한 번에 요약
        if (transcript.length() <= effectiveChunkSize) {
            log.info("Transcript is short. Summarizing in a single call.");
            String prompt = buildPrompt(transcript, agendaText,
                    """
                    다음 [회의 내용]을 2가지 부분으로 요약해줘.
                    
                    1. (요약) 회의의 핵심 결론과 논의 흐름을 10문장 이내로 요약.
                    2. (할 일) 회의에서 발생한 모든 할 일(Action Items)과 결정 사항을 '누가, 무엇을, 언제까지'가 명확히 드러나도록 빠짐없이 리스트업.
                    
                    (두 부분 모두 한글로 작성)
                    """
            );
            return callOpenAI(prompt);
        }

        // Transcript가 긴 경우
        log.info("Transcript is too long ({} chars). Starting Map-Reduce summarization.", transcript.length());

        List<String> chunks = splitText(transcript, effectiveChunkSize);
        StringBuilder partialSummaries = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {
            log.info("Summarizing chunk {}/{}", i + 1, chunks.size());

            // 부분 요약용 프롬프트
            String chunkPrompt = buildPrompt(chunks.get(i), agendaText,
                    "너는 긴 회의록을 부분별로 요약하는 AI야. 아래 [참고 안건]을 바탕으로, 다음 [회의 내용 조각]에서 발생한 [핵심 내용]과 [모든 할 일(Action Items) 및 결정 사항]을 빠짐없이 요약해줘."
            );
            String partialSummary = callOpenAI(chunkPrompt);
            partialSummaries.append(partialSummary).append("\n\n");
        }

        // 부분 요약본들을 합쳐서 최종 요약
        log.info("Combining partial summaries for final summarization.");
        String combinedSummary = partialSummaries.toString();

        // 최종 요약용 프롬프트
        String finalPrompt = buildPrompt(combinedSummary, agendaText,
                """
               다음은 회의록의 [부분별 요약본] 목록이야. [참고 안건]을 바탕으로 이 내용들을 모두 취합해서 2가지 부분으로 [최종 회의 요약본]을 만들어줘.
               
               1. (요약) 전체 회의의 핵심 결론과 논의 흐름을 10문장 이내로 요약.
               2. (할 일) 회의에서 발생한 모든 할 일(Action Items)과 결정 사항을 '누가, 무엇을, 언제까지'가 명확히 드러나도록 빠짐없이 리스트업.
               
               (두 부분 모두 한글로 작성)
               """
        );
        return callOpenAI(finalPrompt);
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
