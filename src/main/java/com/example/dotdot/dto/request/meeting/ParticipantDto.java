package com.example.dotdot.dto.request.meeting;

import lombok.Data;

@Data
public class ParticipantDto {
    private Long userId;
    private String part;
    private Integer speakerIndex;
}
