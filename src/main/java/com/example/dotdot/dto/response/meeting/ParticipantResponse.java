package com.example.dotdot.dto.response.meeting;

import com.example.dotdot.domain.Participant;
import com.example.dotdot.domain.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantResponse {
    private Long userId;
    private String part;
    private Integer speakerIndex;
    private String userName;
    private String profileImageUrl;

    public static ParticipantResponse from(Participant participant) {
        User user = participant.getUser();

        if (user == null) {
            return ParticipantResponse.builder()
                    .userName("알 수 없는 사용자")
                    .build();
        }

        return ParticipantResponse.builder()
                .userId(user.getId())
                .part(participant.getPart())
                .speakerIndex(participant.getSpeakerIndex())
                .userName(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
