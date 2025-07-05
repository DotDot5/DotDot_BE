package com.example.dotdot.controller;

import com.example.dotdot.dto.request.user.PasswordChangeRequest;
import com.example.dotdot.dto.request.user.UserUpdateRequest;
import com.example.dotdot.dto.response.user.UserInfoResponse;
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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "UserController", description = "User 관련 API")
public interface UserControllerSpecification {

    @Operation(summary = "내 정보 조회", description = "인증된 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<DataResponse<UserInfoResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails);


    @Operation(summary = "내 정보 수정", description = "인증된 사용자의 정보를 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 (COMMON-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 이메일 (USER-002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/me")
    public ResponseEntity<DataResponse<UserInfoResponse>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request);


    @Operation(summary = "내 프로필 이미지 수정", description = "인증된 사용자의 프로필 이미지를 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 이미지 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/me/profile-image")
    public ResponseEntity<DataResponse<UserInfoResponse>> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("image") String image);



    @Operation(summary = "내 비밀번호 수정", description = "인증된 사용자의 비밀번호를 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 (COMMON-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "현재 비밀번호 불일치 (USER-005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/me/password")
    public ResponseEntity<DataResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request);


}
