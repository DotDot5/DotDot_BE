package com.example.dotdot.controller;

import com.example.dotdot.domain.task.TaskStatus;
import com.example.dotdot.dto.request.task.ChangeStatusRequest;
import com.example.dotdot.dto.request.task.TaskCreateRequest;
import com.example.dotdot.dto.request.task.TaskUpdateRequest;
import com.example.dotdot.dto.response.task.TaskListResponse;
import com.example.dotdot.dto.response.task.TaskResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import com.example.dotdot.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class TaskController implements TaskControllerSpecification {
    public final TaskService taskService;

    @PostMapping("/teams/{teamId}/tasks")
    public ResponseEntity<DataResponse<Long>> createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        Long taskId = taskService.createTask(userDetails.getId(), teamId, request);
        return ResponseEntity.ok(DataResponse.from(taskId));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<DataResponse<TaskResponse>> getTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId
    ) {
        TaskResponse response = taskService.getTask(userDetails.getId(), taskId);
        return ResponseEntity.ok(DataResponse.from(response));
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<DataResponse<TaskResponse>> updateTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        taskService.updateTask(userDetails.getId(), taskId, request);
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
        return ResponseEntity.ok(DataResponse.ok());
    }

    // ⭐ [수정] 메소드 전체를 아래 코드로 교체합니다.
    @GetMapping("/teams/{teamId}/tasks")
    public ResponseEntity<DataResponse<TaskListResponse>> listTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            // [수정] 'date' 파라미터를 'startDate'로 변경하고, 'endDate'를 추가합니다.
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
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
        // [수정] service를 호출할 때 startDate와 endDate를 모두 전달합니다.
        TaskListResponse resp = taskService.listTasks(
                userDetails.getId(), teamId, startDate, endDate, meetingId, assigneeUserId, pageable
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
        Sort defaultSort = Sort.by(Sort.Order.asc("statusOrder"))
                .and(Sort.by(Sort.Order.desc("priorityOrder")))
                .and(Sort.by(Sort.Order.asc("id")));

        if (sort == null || sort.isBlank()) return defaultSort;

        String[] arr = sort.split(",", 2);
        String key = arr[0].trim().toLowerCase();
        String dirStr = (arr.length > 1 ? arr[1].trim().toLowerCase() : "asc");
        Sort.Direction dir = "desc".equals(dirStr) ? Sort.Direction.DESC : Sort.Direction.ASC;

        String property = switch (key) {
            case "priority" -> "priorityOrder";
            case "status" -> "statusOrder";
            default -> "statusOrder";
        };

        return Sort.by(new Sort.Order(dir, property))
                .and(Sort.by(Sort.Order.asc("id")));
    }
}