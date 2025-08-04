package com.example.dotdot.service;

import com.example.dotdot.domain.Agenda;
import com.example.dotdot.dto.request.chatbot.GptMessage;
import com.example.dotdot.dto.request.chatbot.GptRequest;
import com.example.dotdot.dto.response.chatbot.GptResponse;
import com.example.dotdot.global.exception.AppException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

import static com.example.dotdot.global.exception.chatbot.ChatbotErrorCode.GPT_API_ERROR;
import static com.example.dotdot.global.exception.chatbot.ChatbotErrorCode.GPT_RESPONSE_EMPTY;

@Service
@RequiredArgsConstructor
@Transactional
public class GptService {

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    private final WebClient openAiWebClient;

    public String getGptResponse(List<GptMessage> messages) {
        GptRequest request = new GptRequest(model, messages);

        try {
            GptResponse response = openAiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GptResponse.class)
                    .block();

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new AppException(GPT_RESPONSE_EMPTY);
            }

            return response.getChoices().get(0).getMessage().content();

        } catch (Exception e) {
            throw new AppException(GPT_API_ERROR);
        }
    }

    public static List<GptMessage> buildPrompt(List<Agenda> agendas, String summary, List<GptMessage> history, String question) {
        StringBuilder sb = new StringBuilder(
                "너는 회의 참가자들을 도와주는 똑똑한 챗봇이야. 회의의 흐름을 이해하고, 참가자의 질문에 적절히 응답해야 해.\n" +
                        "아래는 참고할 수 있는 회의 안건과 이전 요약 내용이야. 필요할 때만 참고하고, 질문에 맞춰 친절하고 유연하게 답변해줘.\n\n"
        );

        if (agendas != null && !agendas.isEmpty()) {
            sb.append("회의 안건 목록:\n");
            for (Agenda agenda : agendas) {
                sb.append("- 제목: ").append(agenda.getAgenda()).append("\n");
                if (agenda.getBody() != null && !agenda.getBody().isBlank()) {
                    sb.append("  내용: ").append(agenda.getBody()).append("\n");
                }
            }
        } else {
            sb.append("회의 안건이 등록되어 있지 않습니다.\n");
        }

        if (summary != null && !summary.isBlank()) {
            sb.append("\n이전 대화 요약: ").append(summary).append("\n");
        }

        List<GptMessage> messages = new ArrayList<>();
        messages.add(new GptMessage("system", sb.toString()));
        messages.addAll(history);
        messages.add(new GptMessage("user", question));

        return messages;
    }


    public String summarize(List<GptMessage> messages) {

        // 요약 요청용 system 프롬프트
        List<GptMessage> prompt = new ArrayList<>();
        prompt.add(new GptMessage("system", "지금까지의 대화 내용을 중요한 논의 흐름과 결론 위주로 간결하게 요약해줘."));

        // 이전 메시지들 붙이기
        prompt.addAll(messages);

        // GPT 호출
        return getGptResponse(prompt);
    }
}

