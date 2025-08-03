package com.example.dotdot.dto.response.chatbot;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatResponse {
    private String answer;

    public static ChatResponse of(String answer) {
        return new ChatResponse(answer);
    }
}
