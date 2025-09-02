package com.example.dotdot.dto.request.chatbot;

public record GptMessage(
        String role, // "user", "assistant", "system"
        String content
) {
}
