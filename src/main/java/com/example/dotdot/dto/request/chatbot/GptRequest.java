package com.example.dotdot.dto.request.chatbot;

import java.util.List;

public record GptRequest (
        String model,
        List<GptMessage> messages
) {
}
