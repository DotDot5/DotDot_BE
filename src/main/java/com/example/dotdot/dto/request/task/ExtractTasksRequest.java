package com.example.dotdot.dto.request.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractTasksRequest {
    Boolean dryRun;
    Boolean overwrite;
    Boolean includeAgendas;
    String language;
    Integer defaultDueDays;
}
