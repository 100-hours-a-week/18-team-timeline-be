package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.domain.*;
import com.tamnara.backend.poll.dto.*;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.tamnara.backend.poll.constant.PollResponseMessage.*;
import static com.tamnara.backend.poll.util.PollBuilder.buildPollFromRequest;
import static com.tamnara.backend.poll.util.PollBuilder.buildPollOptionsFromRequest;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;

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

    @Transactional
    public void schedulePoll(Poll poll) {
        poll.changeState(PollState.SCHEDULED);
        pollRepository.save(poll);
    }

    @Transactional
    public void publishPoll(Poll poll) {
        if (pollRepository.existsByState(PollState.PUBLISHED)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    PUBLISHED_POLL_ALREADY_EXISTS
            );
        }
        poll.changeState(PollState.PUBLISHED);
        pollRepository.save(poll);
    }

    @Transactional
    public void deletePoll(Poll poll) {
        poll.changeState(PollState.DELETED);
        pollRepository.save(poll);
    }
}
