package com.example.dotdot.controller;

import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import com.example.dotdot.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController implements BookmarkControllerSpecification{

    private final BookmarkService bookmarkService;

    // 북마크 추가/취소
    @PostMapping("/speech-logs/{speechLogId}")
    public ResponseEntity<DataResponse<Void>> toggleBookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long speechLogId) {
        bookmarkService.toggleBookmark(userDetails.getId(), speechLogId);
        return ResponseEntity.ok(DataResponse.ok());
    }

    // 미팅에서 내가 북마크한 기록들 조회
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<DataResponse<List<Long>>> getMyBookmarksInMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId) {
        List<Long> bookmarks = bookmarkService.findBookmarksByMeeting(userDetails.getId(), meetingId);
        return ResponseEntity.ok(DataResponse.from(bookmarks));
    }
}