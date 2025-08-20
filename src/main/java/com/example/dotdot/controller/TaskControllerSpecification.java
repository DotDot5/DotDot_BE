package com.example.dotdot.controller;

import com.example.dotdot.dto.request.task.ChangeStatusRequest;
import com.example.dotdot.dto.request.task.TaskCreateRequest;
import com.example.dotdot.dto.request.task.TaskUpdateRequest;
import com.example.dotdot.dto.response.task.ExtractTasksResponse;
import com.example.dotdot.dto.response.task.TaskListResponse;
import com.example.dotdot.dto.response.task.TaskResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.dto.ErrorResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "TaskController", description = "Task 관련 API")
@SecurityRequirement(name = "bearerAuth")
public interface TaskControllerSpecification {
    @Operation(summary = "작업 생성", description = "새로운 태스크를 생성합니다.<br>회의에서 추출 시 meetingId 입력<br>priority: HIGH/ MEDIUM/ LOW<br>status: TODO/ PROCESSING/ DONE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 생성 완료"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)/ 존재하지 않는 팀(TEAM-001)/ 존재하지 않는 회의(MEETING-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "팀에 소속되지 않은 담당자 (TASK-005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음 (TEAM-004)/ 해당 팀에 속한 회의 없음(MEETING-002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DataResponse<Long>> createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @Valid @RequestBody TaskCreateRequest request
    );

    @Operation(summary = "작업 상세 조회", description = "taskId를 사용하여 작업 내용을 상세 조회 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)/ 존재하지 않는 팀(TEAM-001)/ 존재하지 않는 회의(MEETING-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음 (TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DataResponse<TaskResponse>> getTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId
    );

    @Operation(summary = "작업 상세 수정", description = "작업 내용을 수정합니다.<br>priority: HIGH/ MEDIUM/ LOW<br>status: TODO/ PROCESSING/ DONE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 상세 수정 완료"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)/ 존재하지 않는 팀(TEAM-001)/ 존재하지 않는 작업(TASK-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "팀에 소속되지 않은 담당자 (TASK-005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음 (TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DataResponse<TaskResponse>> updateTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request
    );

    @Operation(summary = "작업 상태 수정", description = "작업 상태를 수정합니다.<br>status: TODO/ PROCESSING/ DONE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 상태 수정 완료"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)/ 존재하지 않는 팀(TEAM-001)/ 존재하지 않는 작업(TASK-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "팀에 소속되지 않은 담당자 (TASK-005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음 (TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DataResponse<Void>> changeStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody ChangeStatusRequest request
    );

    @Operation(summary = "작업 목록 조회", description = "작업 목록을 조회합니다.<br>날짜/회의ID/담당자 필터링이 가능합니다.<br>정렬 지정이 가능합니다(우선순위, 작업상태 별 오름차순,내림차순)<br>작업 목록, 요약(상태별 개수), 페이징을 보여줍니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 목록 조회"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)/ 존재하지 않는 팀(TEAM-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음 (TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DataResponse<TaskListResponse>> listTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(
                    description = "날짜(ISO-8601). 예: 2025-07-01",
                    schema = @Schema(type = "string", format = "date", example = "2025-07-01")
            )
            LocalDate date,
            @RequestParam(required = false)
            @Parameter(description = "회의 ID(선택). 팀 보드에서는 생략하거나 null", example = "null")
            Long meetingId,
            @RequestParam(required = false)
            @Parameter(description = "담당자 User ID(선택). 전체 팀원은 생략", example = "null")
            Long assigneeUserId,    // null=전체 팀원, 값=특정 팀원
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "")
            @Parameter(
                    description = "정렬: status|priority + asc|desc",
                    schema = @Schema(
                            allowableValues = {"status,asc","status,desc","priority,asc","priority,desc"},
                            example = "status,asc"
                    )
            )
            String sort
    );

    @Operation(summary = "작업 삭제", description = "작업을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 삭제 완료"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)/ 존재하지 않는 작업(TASK-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음 (TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DataResponse<Void>> deleteTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId
    );

    @Operation(summary="회의 태스크 자동 추출", description="회의 transcript(+옵션: agenda)를 기반으로 참가자별 태스크를 추출합니다.")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="추출 성공",
                    content=@Content(mediaType="application/json", schema=@Schema(implementation= ExtractTasksResponse.class))),
            @ApiResponse(responseCode="401", description="인증 필요 (USER-006)",
                    content=@Content(schema=@Schema(implementation=ErrorResponse.class))),
            @ApiResponse(responseCode="404", description="존재하지 않는 회의 (MEETING-001)",
                    content=@Content(schema=@Schema(implementation=ErrorResponse.class)))
    })
    public ResponseEntity<DataResponse<ExtractTasksResponse>> extractTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description="회의 ID", example="11") @PathVariable Long meetingId,
            @RequestBody(required=false) com.example.dotdot.dto.request.task.ExtractTasksRequest request
    );


}
