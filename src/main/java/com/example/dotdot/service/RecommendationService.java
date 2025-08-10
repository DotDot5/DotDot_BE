package com.example.dotdot.service;

import com.example.dotdot.domain.*;
import com.example.dotdot.dto.response.recommend.GoogleSearchResponse;
import com.example.dotdot.global.client.OpenAIRecommendClient;
import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.meeting.MeetingNotFoundException;
import com.example.dotdot.global.exception.search.GoogleSearchErrorCode;
import com.example.dotdot.global.exception.team.ForbiddenTeamAccessException;
import com.example.dotdot.global.exception.user.UserNotFoundException;
import com.example.dotdot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.IntStream;

import static com.example.dotdot.global.exception.meeting.MeetingErrorCode.MEETING_NOT_FOUND;
import static com.example.dotdot.global.exception.team.TeamErrorCode.FORBIDDEN_TEAM_ACCESS;
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
    private final OpenAIRecommendClient openAIRecommendClient;


    //회의 정보를 기반으로 키워드 추출 후 자료 생성
    public List<GoogleSearchResponse> generateRecommendations(Long userId, Long meetingId, int totalLimit) {

        User user = getUserOrThrow(userId);
        Meeting meeting = getMeetingOrThrow(meetingId);
        Team team = meeting.getTeam();
        checkMembershipOrThrow(user, team);

        String meetingSummary=meeting.getSummary();
        List<Agenda> agendas = agendaRepository.findAllByMeetingId(meetingId);

        List<String> queries = Collections.emptyList();

        try{
            queries = openAIRecommendClient.generateSearchQueries(agendas, meetingSummary);

        }catch(Exception e){
            throw new AppException(GoogleSearchErrorCode.GPT_API_ERROR);
        }

        if (queries.isEmpty()) {
            return List.of();
        }
        int perQueryLimit = (int) Math.ceil((double) totalLimit / queries.size());

        List<GoogleSearchResponse> combinedResults = new ArrayList<>();
        for (String query : queries) {
            combinedResults.addAll(googleSearchService.searchResources(query, perQueryLimit));
        }

        return combinedResults.stream()
                .distinct()
                .limit(totalLimit)
                .toList();
    }

    // 추천 자료 저장
    @Transactional
    public void saveRecommendations(Long userId, Long meetingId, List<GoogleSearchResponse> results) {
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
        recommendationRepository.saveAll(recommendations);
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
