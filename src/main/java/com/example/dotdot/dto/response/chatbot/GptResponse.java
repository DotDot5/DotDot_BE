package com.example.dotdot.dto.response.chatbot;

import com.example.dotdot.dto.request.chatbot.GptMessage;
import lombok.Getter;

import java.util.List;

@Getter
public class GptResponse {
    private List<Choice> choices;

    @Getter
    public static class Choice {
        private GptMessage message;

    }

}
