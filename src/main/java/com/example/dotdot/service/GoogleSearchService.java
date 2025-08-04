package com.example.dotdot.service;

import com.example.dotdot.dto.response.recommend.GoogleSearchApiResponse;
import com.example.dotdot.dto.response.recommend.GoogleSearchResponse;
import com.example.dotdot.global.client.GoogleSearchClient;
import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.search.GoogleSearchErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSearchService {
    private final GoogleSearchClient googleSearchClient;

    List<String> trustedDomains = List.of(
            "medium.com",
            "techcrunch.com",
            "zdnet.co.kr",
            "korea.kr",
            "github.com",
            "wikidata.org",
            "tistory.com",
            "brunch.co.kr",
            "velog.io",
            ".go.kr",
            ".edu",
            ".org",
            "ncbi.nlm.nih.gov",
            "spring.io",
            "developer.mozilla.org"
    );

    List<String> importantKeywords = List.of(
            "논문",
            "리포트",
            "보고서",
            "활용사례",
            "백서",
            "서비스",
            "자료",
            "가이드",
            "예제",
            "사용법",
            "API",
            "매뉴얼"
    );
    public List<GoogleSearchResponse> searchResources(String keyword, int limit) {
        log.info("[검색 요청] 키워드: '{}', limit: {}", keyword, limit);
        GoogleSearchApiResponse response = googleSearchClient.search(keyword);

        if (response == null) {
            log.warn("[검색 실패] 응답이 null임");
            throw new AppException(GoogleSearchErrorCode.API_RESPONSE_NULL);
        }

        if (response.getItems() == null || response.getItems().isEmpty()) {
            log.info("[검색 결과 없음] 키워드: '{}'", keyword);
            return List.of(); // 비어 있는 리스트는 UI에서 처리
        }

        List<GoogleSearchApiResponse.Item> items = response.getItems();
        log.info("[검색 결과 개수] {}개", items.size());

        // 점수 계산 및 정렬
        List<ScoredItem> scoredItems = IntStream.range(0, items.size())
                .mapToObj(i -> new ScoredItem(items.get(i), score(items.get(i), i)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .toList();

        // 로그 출력
        for (int i = 0; i < Math.min(5, scoredItems.size()); i++) {
            ScoredItem s = scoredItems.get(i);
            log.info("[{}위] 점수 {} | 제목: {} | 링크: {}", i + 1, s.score, s.item.getTitle(), s.item.getLink());
        }


        List<GoogleSearchResponse> results = scoredItems.stream()
                .map(s -> GoogleSearchResponse.from(s.item))
                .limit(limit)
                .toList();

        return results;

    }

    private double score(GoogleSearchApiResponse.Item item, int index) {
        double score = 0;

        if (isTrusted(item.getLink())) score += 1;

        score += countImportantKeywords(item.getTitle()) * 0.7;

        score += countImportantKeywords(item.getSnippet()) * 0.3;

        score += calculatePositionScore(index);

        return score;
    }

    private boolean isTrusted(String url) {
        return trustedDomains.stream().anyMatch(url::contains);
    }

    private int countImportantKeywords(String text) {
        if (text == null || text.isEmpty()) return 0;
        int count = 0;
        String lowerText = text.toLowerCase();
        for (String keyword : importantKeywords) {
            int index = 0;
            while ((index = lowerText.indexOf(keyword.toLowerCase(), index)) != -1) {
                count++;
                index += keyword.length();
            }
        }
        return count;
    }

    private double calculatePositionScore(int index) {
        return Math.max(0, 0.5 - 0.1 * index);  // 1위: 0.5점, 2위: 0.4점, ...
    }

    private record ScoredItem(GoogleSearchApiResponse.Item item, double score) {}

}
