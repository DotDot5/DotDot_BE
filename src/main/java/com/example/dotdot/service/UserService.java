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
    private final PasswordEncoder passwordEncoder;
    private final Storage storage; // 이미지 업로드를 위한 GCP Storage 사용

    @Value("${spring.cloud.gcp.storage.bucket}") // application.yml에 써둔 bucket 이름
    private String bucketName;

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;

    public UserInfoResponse getUserInfo(Long userId) {
        return UserInfoResponse.from(findUserById(userId));
    }


    public UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = findUserById(userId);
        // 이메일이 변경되었는지 확인
        if (!user.getEmail().equals(request.getEmail())) {
            // 이메일이 변경되었다면, 이미 존재하는 이메일인지 확인
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS);
            }
        }
        // 이메일이 변경되었다면, 이메일 업데이트
        user.updateUserInfo(request);
        return UserInfoResponse.from(user);
    }

    public String updateProfileImage(Long userId, MultipartFile input) {
        User user = findUserById(userId);

        String fileName = UUID.randomUUID().toString(); // UUID를 이용해 고유한 파일 이름 생성
        String ext = input.getContentType();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(ext)
                .build();
        try {
            // GCP Storage에 파일 업로드
            storage.create(blobInfo, input.getInputStream());

            // 업로드된 파일의 URL 생성
            String imageUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName;

            user.updateProfileImageUrl(imageUrl);
            return imageUrl;
        } catch (IOException e) {
            throw new ImageUploadFailException(IMAGE_UPLOAD_FAILED);
        }
    }

    public void updatePassword(Long userId, PasswordChangeRequest request){
        User user = findUserById(userId);
        // 암호화된 저장된 비밀번호와 비밀번호 비교
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));
    }
}
