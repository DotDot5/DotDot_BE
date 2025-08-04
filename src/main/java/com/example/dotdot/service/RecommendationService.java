package com.example.dotdot.service;

import com.example.dotdot.domain.*;
import com.example.dotdot.dto.response.recommend.GoogleSearchResponse;
import com.example.dotdot.global.client.GoogleSearchClient;
import com.example.dotdot.global.exception.meeting.MeetingNotFoundException;
import com.example.dotdot.global.exception.team.ForbiddenTeamAccessException;
import com.example.dotdot.global.exception.team.TeamNotFoundException;
import com.example.dotdot.global.exception.user.UserNotFoundException;
import com.example.dotdot.global.util.KoreanTextProcessor;
import com.example.dotdot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.BreakIterator;
import java.util.*;
import java.util.stream.IntStream;

import static com.example.dotdot.global.exception.meeting.MeetingErrorCode.MEETING_NOT_FOUND;
import static com.example.dotdot.global.exception.team.TeamErrorCode.FORBIDDEN_TEAM_ACCESS;
import static com.example.dotdot.global.exception.team.TeamErrorCode.TEAM_NOT_FOUND;
import static com.example.dotdot.global.exception.user.UserErrorCode.NOT_FOUND;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class RecommendationService {
    private final MeetingRepository meetingRepository;
    private final AgendaRepository agendaRepository;
    private final RecommendationRepository recommendationRepository;
    private final GoogleSearchService googleSearchService;
    private final UserRepository userRepository;
    private final UserTeamRepository userTeamRepository;
    private final KoreanTextProcessor koreanTextProcessor;


    //회의 정보를 기반으로 키워드 추출 후 자료 생성
    public List<GoogleSearchResponse> generateRecommendations(Long userId, Long meetingId, int totalLimit) {
        User user = getUserOrThrow(userId);
        Meeting meeting = getMeetingOrThrow(meetingId);
        Team team = meeting.getTeam();
        checkMembershipOrThrow(user, team);

        List<Agenda> agendas = agendaRepository.findAllByMeetingId(meetingId);

        List<String> queryList = new ArrayList<>();

        Set<String> stopwords = Set.of("의", "가", "이", "은", "는", "들", "과", "도","을", "를", "에", "으로", "에서");

        for (Agenda agenda : agendas) {
            String agendaText = agenda.getAgenda() + " " + Optional.ofNullable(agenda.getBody()).orElse("");
            List<String> sentences = splitIntoSentences(agendaText);
            List<String> keySentences = extractKeySentences(sentences);

            for (String sentence : keySentences) {
                List<String> nouns = koreanTextProcessor.extractNouns(sentence);
                List<String> filtered = nouns.stream()
                        .filter(word -> !stopwords.contains(word))
                        .toList();
                if (!filtered.isEmpty()) {
                    queryList.add(String.join(" ", filtered));
                }
            }
        }

        // 요약도 동일하게 처리
        if (meeting.getSummary() != null && !meeting.getSummary().isBlank()) {
            List<String> summarySentences = splitIntoSentences(meeting.getSummary());
            List<String> keySummarySentences = extractKeySentences(summarySentences);

            for (String sentence : keySummarySentences) {
                List<String> nouns = koreanTextProcessor.extractNouns(sentence);
                List<String> filtered = nouns.stream()
                        .filter(word -> !stopwords.contains(word))
                        .toList();
                if (!filtered.isEmpty()) {
                    queryList.add(String.join(" ", filtered));
                }
            }
        }

        int maxQueryCount = 6;
        queryList = sampleQueriesEvenly(queryList, maxQueryCount);

        int perQueryLimit = Math.max(1, totalLimit / queryList.size());

        List<GoogleSearchResponse> combinedResults = new ArrayList<>();
        for (String query : queryList) {
            combinedResults.addAll(googleSearchService.searchResources(query, perQueryLimit));
        }

        return combinedResults.stream()
                .distinct()
                .limit(totalLimit)
                .toList();
    }

    // 추천 자료 저장
    @Transactional
    public List<Recommendation> saveRecommendations(Long userId, Long meetingId, List<GoogleSearchResponse> results) {
        User user = getUserOrThrow(userId);

        Meeting meeting = getMeetingOrThrow(meetingId);

        Team team = meeting.getTeam();

        checkMembershipOrThrow(user, team);
        List<Recommendation> recommendations = IntStream.range(0, results.size())
                .mapToObj(i -> {
                    GoogleSearchResponse result = results.get(i);
                    return Recommendation.of(
                            result.getTitle(),
                            result.getUrl(),
                            result.getDescription(),
                            meeting,
                            i // priority
                    );
                }).toList();
        return recommendationRepository.saveAll(recommendations);
    }

    @Transactional(readOnly = true)
    public List<GoogleSearchResponse> getRecommendations(Long userId, Long meetingId) {
        User user = getUserOrThrow(userId);
        Meeting meeting = getMeetingOrThrow(meetingId);
        Team team = meeting.getTeam();
        checkMembershipOrThrow(user, team);

        return recommendationRepository.findAllByMeetingOrderByPriorityAsc(meeting)
                .stream()
                .map(r -> GoogleSearchResponse.builder()
                        .title(r.getTitle())
                        .url(r.getUrl())
                        .description(r.getDescription())
                        .build())
                .toList();
    }

    private List<String> sampleQueriesEvenly(List<String> queries, int maxSamples) {
        int size = queries.size();
        if (size <= maxSamples) return queries;

        List<String> sampled = new ArrayList<>(maxSamples);
        double step = (double) size / maxSamples;

        for (int i = 0; i < maxSamples; i++) {
            int index = (int) Math.floor(i * step);
            sampled.add(queries.get(index));
        }
        return sampled;
    }


    public List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.KOREAN);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            sentences.add(text.substring(start, end).trim());
        }
        return sentences;
    }
    public List<String> extractKeySentences(List<String> sentences) {
        List<String> keySentences = new ArrayList<>();
        for (String sentence : sentences) {
            if (sentence.length() > 10) {
                keySentences.add(sentence);
            }
        }
        return keySentences;
    }


    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));
    }

    private Meeting getMeetingOrThrow(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MEETING_NOT_FOUND));
    }

    private void checkMembershipOrThrow(User user, Team team) {
        if (!userTeamRepository.existsByUserAndTeam(user, team)) {
            throw new ForbiddenTeamAccessException(FORBIDDEN_TEAM_ACCESS);
        }
    }
}
