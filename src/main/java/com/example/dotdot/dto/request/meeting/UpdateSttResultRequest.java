package com.example.dotdot.dto.request.meeting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSttResultRequest {

    @NotNull(message = "Audio ID는 필수입니다.")
    private Long audioId;

    @NotNull(message = "Duration은 필수입니다.")
    private int duration;

    @NotBlank(message = "Transcript는 필수입니다.")
    private String transcript;

    private String summary;

}