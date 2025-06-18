package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.domain.Vote;
import com.tamnara.backend.poll.dto.request.VoteRequest;
import com.tamnara.backend.poll.dto.response.PollIdResponse;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.VoteRepository;
import com.tamnara.backend.user.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_ALREADY_VOTED;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_INVALID_SELECTION_COUNT;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_NOT_FOUND;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_NOT_IN_VOTING_PERIOD;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_NOT_PUBLISHED;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_OR_OPTION_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final VoteRepository voteRepository;

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

        return new PollIdResponse(poll.getId());
    }
}
