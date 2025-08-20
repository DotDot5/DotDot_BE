package com.example.dotdot.global.client;

import com.example.dotdot.dto.response.task.TaskDraft;
import com.fasterxml.jackson.databind.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAITaskExtractClient {
    @Value("${openai-tasks.api-key}") private String apiKey;
    @Value("${openai-tasks.base-url}") private String baseUrl;
    @Value("${openai-tasks.model}")    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    public List<TaskDraft> extract(String prompt) {
        try {
            String body = om.writeValueAsString(new OpenAIRequest(model, prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<String> res = restTemplate.postForEntity(baseUrl, new HttpEntity<>(body, headers), String.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                log.error("OpenAI tasks API 실패: {}", res.getStatusCode());
                throw new RuntimeException("GPT_TASKS_API_ERROR");
            }

            JsonNode root = om.readTree(res.getBody());
            String content = root.at("/choices/0/message/content").asText();

            String cleaned = stripCodeFence(content);
            JsonNode parsed = om.readTree(cleaned);

            JsonNode tasksNode;
            if (parsed.isArray()) {
                tasksNode = parsed;
            } else {
                tasksNode = parsed.get("tasks");
            }

            List<TaskDraft> drafts = new ArrayList<>();
            if (tasksNode != null && tasksNode.isArray()) {
                for (JsonNode n : tasksNode) {
                    drafts.add(new TaskDraft(
                            text(n, "assigneeName"),
                            text(n, "title"),
                            text(n, "description"),
                            text(n, "due"),
                            text(n, "priority")
                    ));
                }
            } else {
                // 키가 다르게 올 가능성까지 로그
                log.warn("tasks 배열을 찾지 못함. content={}", content);
            }
            return drafts;

        } catch (Exception e) {
            log.error("태스크 추출 중 오류. baseUrl={}, model={}, prompt={}", baseUrl, model, prompt);
            log.error("원인: {}", e.toString(), e);
            throw new RuntimeException("GPT_TASKS_API_ERROR");
        }
    }

    private static String stripCodeFence(String s) {
        if (s.startsWith("```")) {
            int first = s.indexOf('\n');
            int last = s.lastIndexOf("```");
            if (first >= 0 && last > first) {
                return s.substring(first + 1, last).trim();
            }
        }
        return s;
    }

    // 안전하게 필드값 뽑기
    private static String text(JsonNode n, String f) {
        return (n != null && n.hasNonNull(f)) ? n.get(f).asText() : null;
    }

    static class OpenAIRequest {
        public String model;
        public Message[] messages;
        public Map<String, Object> response_format;
        public Double temperature;

        public OpenAIRequest(String model, String prompt) {
            this.model = model;
            this.messages = new Message[]{
                    new Message("system",
                            "너는 회의록에서 TODO를 추출해 오직 JSON만 출력하는 도우미야. " +
                                    "설명 문장, 마크다운 코드블록 없이 {\"tasks\":[...]} 형태의 JSON 객체만 출력해.")
                    ,
                    new Message("user", prompt)
            };
            this.response_format = Map.of("type", "json_object"); // JSON만 출력 강제
            this.temperature = 0.0; // 결정적 출력
        }
    }

    static class Message {
        public String role, content;
        public Message(String role, String content){ this.role = role; this.content = content; }
    }


}
