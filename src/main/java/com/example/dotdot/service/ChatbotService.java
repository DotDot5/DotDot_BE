package com.example.dotdot.service;

import com.example.dotdot.domain.Agenda;
import com.example.dotdot.dto.request.chatbot.GptMessage;
import com.example.dotdot.repository.AgendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final GptService gptService;
    private final ChatRedisService chatRedisService;
    private final AgendaRepository agendaRepository;

    public String askQuestion(Long meetingId, String question) {
        List<Agenda> agenda = agendaRepository.findAllByMeetingId(meetingId);
        List<GptMessage> promptHistory = chatRedisService.getPromptHistory(meetingId);
        List<GptMessage> messages = GptService.buildPrompt(agenda, null, promptHistory, question);

        String answer = gptService.getGptResponse(messages);

        // 두 히스토리에 모두 저장
        GptMessage userMsg = new GptMessage("user", question);
        GptMessage assistantMsg = new GptMessage("assistant", answer);

        chatRedisService.appendUserHistory(meetingId, userMsg);
        chatRedisService.appendUserHistory(meetingId, assistantMsg);

        chatRedisService.appendPromptHistory(meetingId, userMsg);
        chatRedisService.appendPromptHistory(meetingId, assistantMsg);

        // 오래된 프롬프트는 요약 처리
        chatRedisService.summarizeOldPromptMessages(meetingId, 20, 10);

        return answer;
    }
}

