package com.example.dotdot.controller;

import com.example.dotdot.dto.request.chatbot.ChatRequest;
import com.example.dotdot.dto.request.chatbot.GptMessage;
import com.example.dotdot.dto.response.chatbot.ChatResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.service.ChatRedisService;
import com.example.dotdot.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController implements ChatbotControllerSpecification{

    private final ChatbotService chatbotService;
    private final ChatRedisService  chatRedisService;

    @PostMapping("/ask")
    public ResponseEntity<DataResponse<ChatResponse>> ask(@RequestBody @Valid ChatRequest request) {
        String answer = chatbotService.askQuestion(request.getMeetingId(), request.getQuestion());
        return ResponseEntity.ok(DataResponse.from(ChatResponse.of(answer)));
    }

    @GetMapping("/history")
    public ResponseEntity<DataResponse<List<GptMessage>>> getHistory(@RequestParam Long meetingId) {
        List<GptMessage> messages = chatRedisService.getUserHistory(meetingId);
        return ResponseEntity.ok(DataResponse.from(messages));
    }

    @PostMapping("/end")
    public ResponseEntity<DataResponse<Void>> end(@RequestParam Long meetingId) {
        chatRedisService.setTTL(meetingId, Duration.ofHours(1));
        return ResponseEntity.ok(DataResponse.ok());
    }
}
