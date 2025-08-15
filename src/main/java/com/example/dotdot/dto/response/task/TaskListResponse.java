package com.example.dotdot.dto.response.task;

import io.opencensus.metrics.export.Summary;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListResponse {
    private List<TaskResponse> items;
    private Summary summary;
    private PageMeta page;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Summary {
        private long todo;
        private long processing;
        private long done;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PageMeta {
        private int number;          // page
        private int size;            // size
        private long totalElements;  // 전체 건수
        private int totalPages;      // 전체 페이지 수
    }
}
