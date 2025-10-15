package com.example.dotdot.service;

import com.example.dotdot.domain.User;
import com.example.dotdot.dto.request.user.PasswordChangeRequest;
import com.example.dotdot.dto.request.user.UserUpdateRequest;
import com.example.dotdot.dto.response.user.UserInfoResponse;
import com.example.dotdot.global.exception.user.EmailAlreadyExistsException;
import com.example.dotdot.global.exception.user.ImageUploadFailException;
import com.example.dotdot.global.exception.user.InvalidPasswordException;
import com.example.dotdot.global.exception.user.UserNotFoundException;
import com.example.dotdot.repository.UserRepository;
import com.example.dotdot.repository.UserTeamRepository;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static com.example.dotdot.global.exception.user.UserErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserTeamRepository userTeamRepository;
    private final PasswordEncoder passwordEncoder;
    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;

    public UserInfoResponse getUserInfo(Long userId) {
        return UserInfoResponse.from(findUserById(userId));
    }

    public UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = findUserById(userId);

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS);
            }
        }

        user.updateUserInfo(request);

        if (request.getProfileImageUrl() != null) {
            user.updateProfileImageUrl(request.getProfileImageUrl());
        }

        return UserInfoResponse.from(user);
    }

    public String updateProfileImage(Long userId, MultipartFile input) {
        User user = findUserById(userId);

        String fileName = "profiles/" + UUID.randomUUID().toString();
        String ext = input.getContentType();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(ext)
                .build();
        try {
            storage.create(blobInfo, input.getInputStream());
            String imageUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName;

            user.updateProfileImageUrl(imageUrl);
            return imageUrl;
        } catch (IOException e) {
            throw new ImageUploadFailException(IMAGE_UPLOAD_FAILED);
        }
    }

    public void updatePassword(Long userId, PasswordChangeRequest request){
        User user = findUserById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    public void withdrawal(Long userId) {
        User user = findUserById(userId);
        userTeamRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));
    }
}
