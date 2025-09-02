package com.example.dotdot.controller;

import com.example.dotdot.domain.Recommendation;
import com.example.dotdot.dto.response.recommend.GoogleSearchResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import com.example.dotdot.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class RecommendController implements RecommendControllerSpecification{
    private final RecommendationService recommendationService;

    @PostMapping("/{meetingId}/recommendations")
    public ResponseEntity<DataResponse<Void>> generateAndSave(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "5") int limit
    ){
        List<GoogleSearchResponse> generated = recommendationService.generateRecommendations(userDetails.getId(),meetingId, limit);
        recommendationService.saveRecommendations(userDetails.getId(),meetingId, generated);
        return ResponseEntity.ok(DataResponse.ok());
    }

    @GetMapping("/{meetingId}/recommendations")
    public ResponseEntity<DataResponse<List<GoogleSearchResponse>>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long meetingId
    ) {
        List<GoogleSearchResponse> responses = recommendationService.getRecommendations(userDetails.getId(),meetingId);
        return ResponseEntity.ok(DataResponse.from(responses));
    }


}
