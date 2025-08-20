package com.example.dotdot.controller;

import com.example.dotdot.domain.task.TaskStatus;
import com.example.dotdot.dto.request.task.ChangeStatusRequest;
import com.example.dotdot.dto.request.task.ExtractTasksRequest;
import com.example.dotdot.dto.request.task.TaskCreateRequest;
import com.example.dotdot.dto.request.task.TaskUpdateRequest;
import com.example.dotdot.dto.response.task.ExtractTasksResponse;
import com.example.dotdot.dto.response.task.TaskListResponse;
import com.example.dotdot.dto.response.task.TaskResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import com.example.dotdot.service.TaskService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class TaskController implements TaskControllerSpecification{
    public final TaskService taskService;

    @PostMapping("/teams/{teamId}/tasks")
    public ResponseEntity<DataResponse<Long>> createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @Valid @RequestBody TaskCreateRequest request
    ){
        Long taskId= taskService.createTask(userDetails.getId(),teamId,request);
        return ResponseEntity.ok(DataResponse.from(taskId));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<DataResponse<TaskResponse>> getTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId
    ){
        TaskResponse response=taskService.getTask(userDetails.getId(),taskId);
        return ResponseEntity.ok(DataResponse.from(response));
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<DataResponse<TaskResponse>> updateTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request
    ){
        taskService.updateTask(userDetails.getId(),taskId,request);
        TaskResponse resp = taskService.getTask(userDetails.getId(), taskId);
        return ResponseEntity.ok(DataResponse.from(resp));
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<DataResponse<Void>> changeStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody ChangeStatusRequest request
    ) {
        taskService.changeStatus(userDetails.getId(), taskId, request.getStatus());
        TaskResponse resp = taskService.getTask(userDetails.getId(), taskId);
        return ResponseEntity.ok(DataResponse.ok());
    }

    @GetMapping("/teams/{teamId}/tasks")
    public ResponseEntity<DataResponse<TaskListResponse>> listTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false)
            Long meetingId,
            @RequestParam(required = false)
            Long assigneeUserId,    // null=전체 팀원, 값=특정 팀원
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "")
            String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        TaskListResponse resp = taskService.listTasks(
                userDetails.getId(), teamId, date, meetingId, assigneeUserId, pageable
        );
        return ResponseEntity.ok(DataResponse.from(resp));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<DataResponse<Void>> deleteTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId
    ) {
        taskService.deleteTask(userDetails.getId(), taskId);
        return ResponseEntity.ok(DataResponse.ok());
    }

    private Sort parseSort(String sort) {
        // 기본 정렬: 상태 오름차순(대기→진행→완료) → 우선순위 내림차순(높음→보통→낮음) → id 오름차순
        Sort defaultSort = Sort.by(Sort.Order.asc("statusOrder"))
                .and(Sort.by(Sort.Order.desc("priorityOrder")))
                .and(Sort.by(Sort.Order.asc("id")));

        if (sort == null || sort.isBlank()) return defaultSort;

        String[] arr = sort.split(",", 2);
        String key = arr[0].trim().toLowerCase();
        String dirStr = (arr.length > 1 ? arr[1].trim().toLowerCase() : "asc");
        Sort.Direction dir = "desc".equals(dirStr) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // due 제거: priority/status만 허용
        String property = switch (key) {
            case "priority" -> "priorityOrder"; // @Formula
            case "status"   -> "statusOrder";   // @Formula
            default         -> "statusOrder";   // 잘못된 값이면 상태로 기본
        };

        return Sort.by(new Sort.Order(dir, property))
                .and(Sort.by(Sort.Order.asc("id"))); // tie-breaker
    }

    @PostMapping("/{meetingId}/tasks/extract")
    public ResponseEntity<DataResponse<ExtractTasksResponse>> extractTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId,
            @RequestBody(required = false) ExtractTasksRequest request
    ) {
        var res = taskService.extractFromTranscript(userDetails.getId(), meetingId, request);
        return ResponseEntity.ok(DataResponse.from(res));
    }
}
