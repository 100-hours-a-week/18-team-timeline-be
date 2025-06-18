package com.tamnara.backend.poll.service;

import com.tamnara.backend.alarm.constant.AlarmMessage;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.event.AlarmEvent;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.dto.PollInfoDTO;
import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.response.PollInfoResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tamnara.backend.poll.constant.PollResponseMessage.MIN_CHOICES_EXCEED_MAX;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_NOT_FOUND;
import static com.tamnara.backend.poll.constant.PollResponseMessage.START_DATE_LATER_THAN_END_DATE;
import static com.tamnara.backend.poll.util.PollBuilder.buildPollFromRequest;
import static com.tamnara.backend.poll.util.PollBuilder.buildPollOptionsFromRequest;

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

    @Transactional
    public Long createPoll(PollCreateRequest request) {
        if (request.getMinChoices() > request.getMaxChoices()) {
            throw new IllegalArgumentException(MIN_CHOICES_EXCEED_MAX);
        }
        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new IllegalArgumentException(START_DATE_LATER_THAN_END_DATE);
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
                new PollInfoDTO(poll)
        );
    }

    @Transactional
    public void schedulePoll(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POLL_NOT_FOUND));
        poll.changeState(PollState.SCHEDULED);
        pollRepository.save(poll);
    }

    @Transactional
    public void updatePollStates() {
        Optional<Poll> scheduled = pollRepository.findLatesPollByScheduledPoll();
        if (scheduled.isPresent()) {
            scheduled.get().changeState(PollState.PUBLISHED);
            pollRepository.save(scheduled.get());
        } else {
            log.warn("[WARN] 투표 공개 대상 없음 - 공개 예정인 투표가 존재하지 않음");
            return;
        }

        Optional<Poll> published = pollRepository.findLatestPollByPublishedPoll();
        if (published.isPresent()) {
            published.get().changeState(PollState.DELETED);
            pollRepository.save(published.get());
        } else {
            log.warn("[WARN] 투표 삭제 대상 없음 - 공개 중인 투표가 존재하지 않음");
            return;
        }

        // 알림 이벤트 발행 추가
        publishAlarm(
                userRepository.findAll().stream().map(User::getId).collect(Collectors.toList()),
                AlarmMessage.POLL_START_TITLE,
                String.format(AlarmMessage.POLL_START_CONTENT, scheduled.get().getTitle()),
                AlarmType.POLLS,
                null
        );
    }


    /**
     * 알림 발행 헬퍼 메서드
     */
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
