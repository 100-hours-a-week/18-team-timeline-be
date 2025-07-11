package com.tamnara.backend.poll.service;

import com.tamnara.backend.alarm.constant.AlarmMessage;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.event.AlarmEvent;
import com.tamnara.backend.poll.domain.*;
import com.tamnara.backend.poll.dto.OptionResult;
import com.tamnara.backend.poll.dto.PollInfoDTO;
import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.request.VoteRequest;
import com.tamnara.backend.poll.dto.response.PollIdResponse;
import com.tamnara.backend.poll.dto.response.PollInfoResponse;
import com.tamnara.backend.poll.dto.response.PollStatisticsResponse;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.VoteRepository;
import com.tamnara.backend.poll.repository.VoteStatisticsRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tamnara.backend.poll.constant.PollResponseMessage.*;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_ALREADY_VOTED;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_INVALID_SELECTION_COUNT;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_NOT_IN_VOTING_PERIOD;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_OR_OPTION_NOT_FOUND;
import static com.tamnara.backend.poll.util.PollBuilder.buildPollFromRequest;
import static com.tamnara.backend.poll.util.PollBuilder.buildPollOptionsFromRequest;
import static com.tamnara.backend.poll.util.VoteStatisticsBuilder.buildNew;
import static com.tamnara.backend.poll.util.VoteStatisticsBuilder.buildUpdated;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {

    private final ApplicationEventPublisher eventPublisher;

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final VoteRepository voteRepository;
    private final VoteStatisticsRepository voteStatisticsRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Long createPoll(PollCreateRequest request) {
        log.info("[POLL] createPoll 시작 - title:{}", request.getTitle());
        if (request.getMinChoices() > request.getMaxChoices()) {
            throw new IllegalArgumentException(MIN_CHOICES_EXCEED_MAX);
        }

        Poll poll = buildPollFromRequest(request);
        Poll savedPoll = pollRepository.save(poll);
        log.info("[POLL] createPoll 처리 중 - 투표 저장 성공, title:{}", request.getTitle());

        List<PollOption> options = buildPollOptionsFromRequest(request.getOptions(), savedPoll);
        pollOptionRepository.saveAll(options);
        log.info("[POLL] createPoll 처리 중 - 투표 선택지 저장 성공, title:{}", request.getTitle());

        log.info("[POLL] createPoll 완료 - title:{}", request.getTitle());
        return savedPoll.getId();
    }

    public Poll getPollById(Long pollId) {
        log.info("[POLL] getPollById 시작 - pollId:{}", pollId);

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));

        log.info("[POLL] getPollById 완료 - pollId:{}", pollId);
        return poll;
    }

    public PollInfoResponse getLatestPublishedPoll(Long userId) {
        log.info("[POLL] getLatestPublishedPoll 시작 - userId:{}", userId);

        Poll poll = pollRepository.findLatestPollByPublishedPoll()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));

        log.info("[POLL] getLatestPublishedPoll 완료 - userId:{}", userId);
        return new PollInfoResponse(
                voteRepository.hasVotedLatestPublishedPoll(userId),
                voteRepository.findVotedOptionIdsOfLatestPublishedPoll(userId),
                new PollInfoDTO(poll)
        );
    }

    @Override
    @Transactional
    public void updatePollStates() {
        log.info("[POLL] updatePollStates 시작");

        Optional<Poll> scheduled = pollRepository.findLatesPollByScheduledPoll();
        if (scheduled.isPresent()) {
            scheduled.get().changeState(PollState.PUBLISHED);
            scheduled.get().updateStartAt(LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0));
            scheduled.get().updateEndAt(
                    LocalDateTime.now()
                            .plusWeeks(1)
                            .withHour(9)
                            .withMinute(30)
                            .withSecond(0)
                            .withNano(0)
            );

            pollRepository.save(scheduled.get());

            // 알림 이벤트 발행 추가
            publishAlarm(
                    userRepository.findAll().stream().map(User::getId).collect(Collectors.toList()),
                    AlarmMessage.POLL_START_TITLE,
                    String.format(AlarmMessage.POLL_START_CONTENT, scheduled.get().getTitle()),
                    AlarmType.POLLS,
                    null
            );
        } else {
            log.warn("[POLL] updatePollStates 경고 - 투표 공개 대상 없음(공개 예정인 투표가 존재하지 않음)");
            return;
        }
        log.info("[POLL] updatePollStates 처리 중 - 신규 투표 공개 및 투표 알림 이벤트 발행 성공");

        Optional<Poll> published = pollRepository.findLatestPollByPublishedPoll();
        if (published.isPresent()) {
            published.get().changeState(PollState.DELETED);
            pollRepository.save(published.get());
        } else {
            log.warn("[POLL] updatePollStates 경고 - 투표 삭제 대상 없음(공개 중인 투표가 존재하지 않음)");
        }
        log.info("[POLL] updatePollStates 처리 중 - 기존 투표 삭제 성공");

        log.info("[POLL] updatePollStates 완료");
    }

    @Override
    @Transactional
    public PollIdResponse vote(User user, VoteRequest voteRequest) {
        log.info("[POLL] vote 시작 - userId:{}", user.getId());

        Poll poll = pollRepository.findLatestPollByPublishedPoll()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));

        if (poll.getState() != PollState.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, POLL_NOT_PUBLISHED);
        }

        LocalDateTime now = LocalDateTime.now();
        if (poll.getStartAt().isAfter(now) || poll.getEndAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, POLL_NOT_IN_VOTING_PERIOD);
        }
        log.info("[POLL] vote 처리 중 - 투표 유효성 확인, userId:{}", user.getId());

        if (voteRepository.hasVotedLatestPublishedPoll(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, POLL_ALREADY_VOTED);
        }
        log.info("[POLL] vote 처리 중 - 투표 가능 여부 확인, userId:{}", user.getId());

        List<PollOption> options = pollOptionRepository.findAllById(voteRequest.getOptionIds());
        if (options.size() != voteRequest.getOptionIds().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_OR_OPTION_NOT_FOUND);
        }

        boolean allMatch = options.stream().allMatch(option -> option.getPoll().getId().equals(poll.getId()));
        if (!allMatch) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_OR_OPTION_NOT_FOUND);
        }

        if (voteRequest.getOptionIds().size() < poll.getMinChoices() || voteRequest.getOptionIds().size() > poll.getMaxChoices()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, POLL_INVALID_SELECTION_COUNT);
        }
        log.info("[POLL] vote 처리 중 - 투표 선택지 유효성 확인, userId:{}", user.getId());

        List<Vote> votes = options.stream()
                .map(option -> Vote.builder()
                        .poll(poll)
                        .user(user)
                        .option(option)
                        .votedAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();
        voteRepository.saveAll(votes);
        log.info("[POLL] vote 처리 중 - 투표 기록 저장 성공, userId:{}", user.getId());

        generateAllStatistics();
        log.info("[POLL] vote 처리 중 - 투표 결과 통계 업데이트 성공, userId:{}", user.getId());

        log.info("[POLL] vote 완료- 투표 선택지 유효성 확인, userId:{}", user.getId());
        return new PollIdResponse(poll.getId());
    }

    @Override
    @Transactional
    public void schedulePoll(Long pollId) {
        log.info("[POLL] schedulePoll 시작 - pollId:{}", pollId);

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));
        log.info("[POLL] schedulePoll 처리 중 - 투표 유효성 확인, pollId:{}", pollId);

        if (poll.getState() != PollState.DRAFT) {
            log.warn("[POLL] schedulePoll 실패 - 투표의 상태가 SCHEDULED 전환 대상이 아님, pollId:{} pollState:{}", pollId, poll.getState());
            throw new ResponseStatusException(HttpStatus.CONFLICT, POLL_SCHEDULED_CONFLICT);
        }
        poll.changeState(PollState.SCHEDULED);
        pollRepository.save(poll);
        log.info("[POLL] schedulePoll 처리 중 - 투표 상태를 SCHEDULED로 전환 성공, pollId:{}", pollId);

        log.info("[POLL] schedulePoll 완료 - pollId:{}", pollId);
    }

    @Override
    public PollStatisticsResponse getVoteStatistics(Long pollId) {
        log.info("[POLL] pollStatisticsReponse 시작 - pollId:{}", pollId);

        pollRepository.findById(pollId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));
        log.info("[POLL] pollStatisticsReponse 처리 중 - 투표 유효성 확인, pollId:{}", pollId);

        List<VoteStatistics> voteStatisticsList = voteStatisticsRepository.findByPollId(pollId);
        log.info("[POLL] pollStatisticsReponse 처리 중 - 투표 결과 통계 조회 성공, pollId:{}", pollId);

        List<OptionResult> results = new ArrayList<>();
        Long totalVotes = 0L;
        for (VoteStatistics vs : voteStatisticsList) {
            OptionResult optionResult = new OptionResult(
                    vs.getOption().getId(),
                    vs.getOption().getTitle(),
                    vs.getCount()
            );
            results.add(optionResult);
            totalVotes += vs.getCount();
        }
        results.sort(Comparator.comparing(OptionResult::getCount).reversed());
        log.info("[POLL] pollStatisticsReponse 처리 중 - 투표 결과 통계 응답 생성 성공, pollId:{}", pollId);

        log.info("[POLL] pollStatisticsReponse 완료 - pollId:{}", pollId);
        return new PollStatisticsResponse(
                pollId,
                results,
                totalVotes
        );
    }


    /**
     * 헬퍼 메서드
     */

    private void generateAllStatistics() {
        log.info("[POLL] generateAllStatistics 시작");

        List<Poll> polls = pollRepository.findByState(PollState.PUBLISHED);
        for (Poll poll : polls) {
            for (PollOption option : poll.getOptions()) {
                long count = voteRepository.countByPollIdAndOptionId(poll.getId(), option.getId());

                VoteStatistics updated = voteStatisticsRepository.findByPollIdAndOptionId(poll.getId(), option.getId())
                        .map(existing -> buildUpdated(existing, count))
                        .orElseGet(() -> buildNew(poll, option, count));

                voteStatisticsRepository.save(updated);
            }
        }

        log.info("[POLL] generateAllStatistics 완료");
    }

    private void publishAlarm(List<Long> userIdList, String title, String content, AlarmType targetType, Long targetId) {
        AlarmEvent event = new AlarmEvent(
                userIdList,
                title,
                content,
                targetType,
                targetId
        );
        eventPublisher.publishEvent(event);
    }
}
