// SpeechLogDto.java

package com.example.dotdot.dto.request.meeting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeechLogDto {
    private Long speechLogId;
    private int speakerIndex;
    private String text;
    private int startTime;
    private int endTime;
}