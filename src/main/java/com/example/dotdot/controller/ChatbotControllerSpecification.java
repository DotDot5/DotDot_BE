package com.example.dotdot.controller;

import com.example.dotdot.dto.request.chatbot.ChatRequest;
import com.example.dotdot.dto.request.chatbot.GptMessage;
import com.example.dotdot.dto.response.chatbot.ChatResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "ChatbotController", description = "Chatbot 관련 API")
public interface ChatbotControllerSpecification {

    @Operation(summary = "챗봇 질문하기", description = "회의에 대해 챗봇에게 질문을 보냅니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "질문 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 (COMMON-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "GPT 응답이 비어 있습니다. (GPT-001) / GPT 호출 중 오류가 발생했습니다. (GPT-002) / Redis 작업 중 오류가 발생했습니다. (REDIS-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/ask")
    ResponseEntity<DataResponse<ChatResponse>> ask(@RequestBody ChatRequest request);


    @Operation(summary = "대화 히스토리 조회", description = "해당 회의의 전체 대화 내용을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "Redis 작업 중 오류가 발생했습니다. (REDIS-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/history")
    ResponseEntity<DataResponse<List<GptMessage>>> getHistory(@RequestParam Long meetingId);


    @Operation(summary = "대화 종료", description = "회의의 대화를 종료하고 TTL(1시간)을 설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "종료 성공"),
            @ApiResponse(responseCode = "500", description = "Redis 작업 중 오류가 발생했습니다. (REDIS-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/end")
    ResponseEntity<DataResponse<Void>> end(@RequestParam Long meetingId);
}
