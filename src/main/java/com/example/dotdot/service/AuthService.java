package com.example.dotdot.service;

import com.example.dotdot.domain.PasswordResetToken;
import com.example.dotdot.domain.User;
import com.example.dotdot.dto.request.user.LoginRequest;
import com.example.dotdot.dto.request.user.PasswordUpdateRequest;
import com.example.dotdot.dto.request.user.SignupRequest;
import com.example.dotdot.dto.response.user.TokenResponse;
import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.user.EmailAlreadyExistsException;
import com.example.dotdot.global.exception.user.InvalidCredentialsException;
import com.example.dotdot.global.exception.user.InvalidRefreshTokenException;
import com.example.dotdot.global.exception.user.UserNotFoundException;
import com.example.dotdot.global.security.JwtTokenProvider;
import com.example.dotdot.repository.PasswordResetTokenRepository;
import com.example.dotdot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.example.dotdot.global.exception.CommonErrorCode.INTERNAL_SERVER_ERROR;
import static com.example.dotdot.global.exception.user.UserErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender javaMailSender;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // 이메일 중복 확인 메소드
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }


    public void signup(SignupRequest request) {
        // 이미 가입한 이메일인지 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        userRepository.save(request.toEntity(encodedPassword));
    }

    public TokenResponse login(LoginRequest request) {
        try {
            String email = request.getEmail();
            String password = request.getPassword();

            // AuthenticationManager를 통한 인증 처리
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            User user = findUserByEmail(email);

            // 인증 성공 시 토큰 생성 로직
            String accessToken = jwtTokenProvider.createToken(user.getId());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

            // 사용자 조회

            user.updateRefreshToken(refreshToken);

            return new TokenResponse(accessToken, refreshToken);

        } catch (AuthenticationException e) {
            // 이메일이나 비밀번호가 잘못된 경우
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        } catch (Exception e) {
            // 기타 예외 처리
            throw new AppException(INTERNAL_SERVER_ERROR);
        }
    }


    // RefreshToken을 받아 Access Token 재발급
    public String reissue(String refreshToken) {

        // Refresh Token 유효성 검사
        // JWT 토큰이 유효하지 않거나 만료된 경우 예외 발생
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException(INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = findUserById(userId);

        // Refresh Token이 사용자 정보와 일치하는지 확인
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidRefreshTokenException(INVALID_REFRESH_TOKEN);
        }

        return jwtTokenProvider.createToken(user.getId());
    }


    public void logout(Long userId) {
        User user = findUserById(userId);
        user.updateRefreshToken(null); // Refresh Token 무효화
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));
    }



    // 비밀번호 재설정 요청
    public void createPasswordResetToken(String email) {
        User user = findUserByEmail(email);
        Optional<PasswordResetToken> existingTokenOpt = passwordResetTokenRepository.findByUser(user);

        String newToken = UUID.randomUUID().toString();
        LocalDateTime newExpiryDate = LocalDateTime.now().plusMinutes(10);

        if (existingTokenOpt.isPresent()) {
            // 토큰이 이미 존재한다면, 기존 토큰의 정보만 갱신
            PasswordResetToken existingToken = existingTokenOpt.get();
            existingToken.updateToken(newToken, newExpiryDate);
            passwordResetTokenRepository.save(existingToken);
        } else {
            // 토큰이 없다면, 새로 생성
            PasswordResetToken newPasswordResetToken = PasswordResetToken.of(newToken, user, newExpiryDate);
            passwordResetTokenRepository.save(newPasswordResetToken);
        }
        sendPasswordResetEmail(user.getEmail(), newToken);
    }

    // 비밀번호 재설정 이메일 전송
    @Async
    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("DotDot 비밀번호 재설정 요청");
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            message.setText("비밀번호를 재설정하려면 다음 링크를 클릭하세요: " + resetUrl);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new AppException(PASSWORD_RESET_EMAIL_SEND_FAILED);
        }
    }

    public void resetPassword(PasswordUpdateRequest request) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(INVALID_PASSWORD_RESET_TOKEN));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(PASSWORD_RESET_TOKEN_EXPIRED);
        }

        User user = resetToken.getUser();
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 사용 완료된 토큰 삭제
        passwordResetTokenRepository.delete(resetToken);
    }
}
