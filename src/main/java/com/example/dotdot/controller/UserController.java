package com.example.dotdot.controller;

import com.example.dotdot.dto.request.user.PasswordChangeRequest;
import com.example.dotdot.dto.request.user.UserUpdateRequest;
import com.example.dotdot.dto.response.user.UserInfoResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import com.example.dotdot.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "User 관련 API")
public class UserController implements UserControllerSpecification{
    private final UserService userService;


    @GetMapping("/me")
    public ResponseEntity<DataResponse<UserInfoResponse>> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(DataResponse.from(userInfo));
    }


    @PutMapping("/me")
    public ResponseEntity<DataResponse<UserInfoResponse>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        Long userId = userDetails.getId();
        UserInfoResponse updatedUserInfo = userService.updateUserInfo(userId, request);
        return ResponseEntity.ok(DataResponse.from(updatedUserInfo));
    }


    @PutMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DataResponse<String>> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        Long userId = userDetails.getId();
        String imageUrl = userService.updateProfileImage(userId, file);
        return ResponseEntity.ok(DataResponse.from(imageUrl));
    }


    @PutMapping("/me/password")
    public ResponseEntity<DataResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request) {
        Long userId = userDetails.getId();
        userService.updatePassword(userId, request);
        return ResponseEntity.ok(DataResponse.ok());
    }


    @DeleteMapping("/me/withdrawal")
    public ResponseEntity<DataResponse<Void>> withdrawal(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        userService.withdrawal(userId);
        return ResponseEntity.ok(DataResponse.ok());
    }
}