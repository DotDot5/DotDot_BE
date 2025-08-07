package com.example.dotdot.dto.request.meeting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SttResultUpdateRequest {
    private Integer duration;
    private String transcript;
}