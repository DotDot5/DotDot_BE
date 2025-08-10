package com.example.dotdot.global.client;

import com.example.dotdot.dto.response.recommend.GoogleSearchApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleSearchClient {


    @Value("${google.api-key}")
    private String googleApiKey;

    @Value("${google.cx-id}")
    private String cxId;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://www.googleapis.com")
            .build();

    public GoogleSearchApiResponse search(String query) {
        String url= UriComponentsBuilder
                .fromPath("/customsearch/v1")
                .queryParam("key",googleApiKey)
                .queryParam("cx",cxId)
                .queryParam("q",query)
                .build().toUriString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(GoogleSearchApiResponse.class)
                .block();
    }


}
