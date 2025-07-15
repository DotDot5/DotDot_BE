package com.example.dotdot.dto.request.meeting;

import com.example.dotdot.domain.Participant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDto {
    private Long userId;
    private String part;
    private Integer speakerIndex;
    private String userName;

    public static ParticipantDto from(Participant participant) {
        return ParticipantDto.builder()
                .userId(participant.getUser().getId())
                .part(participant.getPart())
                .speakerIndex(participant.getSpeakerIndex())
                .userName(participant.getUser().getName())
                .build();
    }
}
