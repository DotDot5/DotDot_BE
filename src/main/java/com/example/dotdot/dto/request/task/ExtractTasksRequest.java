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
    Boolean dryRun; //DB 저장 여부
    Boolean overwrite; // 덮어쓰기 여부
    Boolean includeAgendas; // 안건 포함 여부
    String language; // ko || en
    Integer defaultDueDays;
}
