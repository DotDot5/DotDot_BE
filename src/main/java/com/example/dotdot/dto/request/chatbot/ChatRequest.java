package com.example.dotdot.dto.request.chatbot;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChatRequest {
    private Long meetingId;
    @NotBlank(message = "채팅을 입력하세요.")
    private String question;
}
