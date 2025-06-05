package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.domain.*;
import com.tamnara.backend.poll.dto.*;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return pollRepository.findById(pollId).orElse(null);
    }
}
