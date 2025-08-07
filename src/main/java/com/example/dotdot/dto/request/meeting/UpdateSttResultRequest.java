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

    // Meeting 엔티티의 audioId가 Long 타입이므로 Long으로 매핑
    @NotNull(message = "Audio ID는 필수입니다.")
    private Long audioId;

    // Meeting 엔티티의 duration이 int 타입이므로 int로 매핑
    @NotNull(message = "Duration은 필수입니다.")
    private int duration;

    // STT 전문
    @NotBlank(message = "Transcript는 필수입니다.")
    private String transcript;

    // STT 요약 (필수 아님)
    private String summary;

    // 필요하다면 note 필드도 추가할 수 있습니다. (Meeting 엔티티에 있음)
    // private String note;
}