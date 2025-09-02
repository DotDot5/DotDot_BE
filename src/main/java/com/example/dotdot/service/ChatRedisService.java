package com.example.dotdot.service;

import com.example.dotdot.dto.request.chatbot.GptMessage;
import com.example.dotdot.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static com.example.dotdot.global.exception.chatbot.ChatbotErrorCode.REDIS_ERROR;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final GptService gptService;

    private String userHistoryKey(Long meetingId) {
        return "chat:history:" + meetingId;
    }

    private String promptHistoryKey(Long meetingId) {
        return "chat:prompt:" + meetingId;
    }

    private String summaryKey(Long meetingId) {
        return "chat:summary:" + meetingId;
    }

    // 사용자용 전체 메시지 추가
    public void appendUserHistory(Long meetingId, GptMessage message) {
        try {
            redisTemplate.opsForList().rightPush(userHistoryKey(meetingId), message);
        } catch (DataAccessException e) {
            throw new AppException(REDIS_ERROR);
        }
    }

    // GPT 프롬프트용 메시지 추가
    public void appendPromptHistory(Long meetingId, GptMessage message) {
        try {
            redisTemplate.opsForList().rightPush(promptHistoryKey(meetingId), message);
        } catch (DataAccessException e) {
            throw new AppException(REDIS_ERROR);
        }
    }

    // 전체 대화 조회 (화면용)
    public List<GptMessage> getUserHistory(Long meetingId) {
        try {
            List<Object> raw = redisTemplate.opsForList().range(userHistoryKey(meetingId), 0, -1);
            return castToMessages(raw != null ? raw : List.of());
        } catch (DataAccessException e) {
            throw new AppException(REDIS_ERROR);
        }
    }

    // GPT 프롬프트 대화 조회
    public List<GptMessage> getPromptHistory(Long meetingId) {
        try {
            List<Object> raw = redisTemplate.opsForList().range(promptHistoryKey(meetingId), 0, -1);
            return castToMessages(raw != null ? raw : List.of());
        } catch (DataAccessException e) {
            throw new AppException(REDIS_ERROR);
        }
    }

    // 오래된 프롬프트 메시지 요약 처리
    public void summarizeOldPromptMessages(Long meetingId, int threshold, int cutoff) {
        try {
            Long size = redisTemplate.opsForList().size(promptHistoryKey(meetingId));
            if (size != null && size >= threshold) {
                List<Object> old = redisTemplate.opsForList().range(promptHistoryKey(meetingId), 0, cutoff - 1);
                redisTemplate.opsForList().trim(promptHistoryKey(meetingId), cutoff, -1);

                String summary = summarize(old);
                redisTemplate.opsForList().leftPush(promptHistoryKey(meetingId), new GptMessage("system", summary));
                redisTemplate.opsForValue().set(summaryKey(meetingId), summary);
            }
        } catch (DataAccessException e) {
            throw new AppException(REDIS_ERROR);
        }
    }

    public void setTTL(Long meetingId, Duration ttl) {
        try {
            redisTemplate.expire(userHistoryKey(meetingId), ttl);
            redisTemplate.expire(promptHistoryKey(meetingId), ttl);
            redisTemplate.expire(summaryKey(meetingId), ttl);
        } catch (DataAccessException e) {
            throw new AppException(REDIS_ERROR);
        }
    }

    private List<GptMessage> castToMessages(List<Object> raw) {
        return raw.stream()
                .filter(o -> o instanceof GptMessage)
                .map(o -> (GptMessage) o)
                .toList();
    }

    private String summarize(List<Object> messages) {
        List<GptMessage> gptMessages = castToMessages(messages);
        return gptService.summarize(gptMessages);
    }
}
