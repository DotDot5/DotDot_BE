package com.example.dotdot.service;

import com.example.dotdot.domain.Bookmark;
import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.SpeechLog;
import com.example.dotdot.domain.User;
import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.meeting.MeetingErrorCode;
import com.example.dotdot.global.exception.meeting.MeetingNotFoundException;
import com.example.dotdot.repository.BookmarkRepository;
import com.example.dotdot.repository.MeetingRepository;
import com.example.dotdot.repository.SpeechLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.dotdot.global.exception.bookmark.BookmarkErrorCode.SPEECH_LOG_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserService userService;
    private final SpeechLogRepository speechLogRepository;
    private final MeetingRepository meetingRepository;

    public void toggleBookmark(Long userId, Long speechLogId) {
        User user = userService.findUserById(userId);
        SpeechLog speechLog = speechLogRepository.findById(speechLogId).orElseThrow(() -> new AppException(SPEECH_LOG_NOT_FOUND));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndSpeechLog(user, speechLog);
        if(existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
        } else {
            bookmarkRepository.save(Bookmark.of(user, speechLog));
        }
    }

    public List<Long> findBookmarksByMeeting(Long userId, Long meetingId) {
        User user = userService.findUserById(userId);
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));

        return bookmarkRepository.findBookmarkedSpeechLogsByUserAndMeeting(user.getId(), meeting.getId());
    }

}
