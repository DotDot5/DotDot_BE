package com.example.dotdot.controller;

import com.example.dotdot.domain.Recommendation;
import com.example.dotdot.dto.response.recommend.GoogleSearchResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.dto.ErrorResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recommend", description = "회의 관련 자료 추천 API")
public interface RecommendControllerSpecification {

    @Operation(summary = "자료 추천", description = "Google API를 사용하여 키워드를 검색한 뒤 자료를 추천합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자료 추천 및 저장 완료"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001) / 존재하지 않는 회의 (MEETING-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Google 검색 응답이 null (GOOGLE-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DataResponse<Void>> generateAndSave(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "5") int limit
    );


    @Operation(summary = "자료 추천 조회", description = "추천된 자료를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자료 추천 조회 완료"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001) / 존재하지 않는 회의 (MEETING-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DataResponse<List<GoogleSearchResponse>>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long meetingId
    ) ;
}
