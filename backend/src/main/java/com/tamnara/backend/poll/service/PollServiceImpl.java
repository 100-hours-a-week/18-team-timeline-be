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
        if (request.getMinChoices() > request.getMaxChoices()) {
            throw new IllegalArgumentException(MIN_CHOICES_EXCEED_MAX);
        }

        Poll poll = buildPollFromRequest(request);
        Poll savedPoll = pollRepository.save(poll);

        List<PollOption> options = buildPollOptionsFromRequest(request.getOptions(), savedPoll);
        pollOptionRepository.saveAll(options);

        return savedPoll.getId();
    }

    public Poll getPollById(Long pollId) {
        return pollRepository.findById(pollId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));
    }

    public PollInfoResponse getLatestPublishedPoll(Long userId) {
        Poll poll = pollRepository.findLatestPollByPublishedPoll()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));

        return new PollInfoResponse(
                voteRepository.hasVotedLatestPublishedPoll(userId),
                voteRepository.findVotedOptionIdsOfLatestPublishedPoll(userId),
                new PollInfoDTO(poll)
        );
    }

    @Override
    @Transactional
    public void updatePollStates() {
        Optional<Poll> published = pollRepository.findLatestPollByPublishedPoll();
        if (published.isPresent()) {
            published.get().changeState(PollState.DELETED);
            pollRepository.save(published.get());
        } else {
            log.warn("[WARN] 투표 삭제 대상 없음 - 공개 중인 투표가 존재하지 않음");
        }

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
            log.warn("[WARN] 투표 공개 대상 없음 - 공개 예정인 투표가 존재하지 않음");
        }
    }

    @Override
    @Transactional
    public PollIdResponse vote(User user, VoteRequest voteRequest) {
        // 1. 투표가 유효한지 체크
        Poll poll = pollRepository.findLatestPollByPublishedPoll()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));

        // 2. 투표 상태 확인
        if (poll.getState() != PollState.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, POLL_NOT_PUBLISHED);
        }

        // 3. 투표 기간 확인
        LocalDateTime now = LocalDateTime.now();
        if (poll.getStartAt().isAfter(now) || poll.getEndAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, POLL_NOT_IN_VOTING_PERIOD);
        }

        // 4. 선택된 옵션들이 해당 투표에 속하는지 확인
        List<PollOption> options = pollOptionRepository.findAllById(voteRequest.getOptionIds());
        if (options.size() != voteRequest.getOptionIds().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_OR_OPTION_NOT_FOUND);
        }

        boolean allMatch = options.stream().allMatch(option -> option.getPoll().getId().equals(poll.getId()));
        if (!allMatch) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_OR_OPTION_NOT_FOUND);
        }

        // 5. 선택된 옵션 수가 min_choices ~ max_choices 범위에 포함되는지 확인
        if (voteRequest.getOptionIds().size() < poll.getMinChoices() || voteRequest.getOptionIds().size() > poll.getMaxChoices()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, POLL_INVALID_SELECTION_COUNT);
        }

        // 6. 이미 투표한 사용자인지 확인
        if (voteRepository.hasVotedLatestPublishedPoll(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, POLL_ALREADY_VOTED);
        }

        // 7. 투표 기록 저장
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
        generateAllStatistics();

        return new PollIdResponse(poll.getId());
    }

    @Override
    @Transactional
    public void schedulePoll(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));

        if (poll.getState() != PollState.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, POLL_SCHEDULED_CONFLICT);
        }

        poll.changeState(PollState.SCHEDULED);
        pollRepository.save(poll);
    }

    @Override
    public PollStatisticsResponse getVoteStatistics(Long pollId) {
        pollRepository.findById(pollId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));

        List<VoteStatistics> voteStatisticsList = voteStatisticsRepository.findByPollId(pollId);

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
