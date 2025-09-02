package com.example.dotdot.dto.request.meeting;

import com.example.dotdot.dto.request.meeting.SpeechLogDto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SttResultUpdateRequest {
    private int duration;
    private String transcript;
    private String audio_id;
    private List<SpeechLogDto> speechLogs;
}