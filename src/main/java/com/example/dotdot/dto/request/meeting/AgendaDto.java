package com.example.dotdot.dto.request.meeting;

import com.example.dotdot.domain.Agenda;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendaDto {
    private String agenda;
    private String body;

    public static AgendaDto from(Agenda agenda) {
        return AgendaDto.builder()
                .agenda(agenda.getAgenda())
                .body(agenda.getBody())
                .build();
    }
}
