package com.example.dotdot.dto.response.recommend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleSearchResponse {
    private String title;
    private String url;
    private String description;

    public static GoogleSearchResponse from(GoogleSearchApiResponse.Item item) {
        return GoogleSearchResponse.builder()
                .title(item.getTitle())
                .url(item.getLink())
                .description(item.getSnippet())  // 또는 item.getDescription() 등
                .build();
    }
}
