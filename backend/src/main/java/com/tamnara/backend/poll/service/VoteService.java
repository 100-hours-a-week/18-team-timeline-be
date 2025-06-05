package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.domain.*;
import com.tamnara.backend.poll.exception.PollBadRequestException;
import com.tamnara.backend.poll.exception.PollConflictException;
import com.tamnara.backend.poll.exception.PollForbiddenException;
import com.tamnara.backend.poll.exception.PollNotFoundException;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import com.tamnara.backend.poll.repository.VoteRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.tamnara.backend.poll.constant.PollResponseMessage.*;

@Service
public class VoteService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    @Autowired
    public VoteService(PollRepository pollRepository, PollOptionRepository pollOptionRepository,
                           VoteRepository voteRepository, UserRepository userRepository) {
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void vote(Long pollId, User user, List<Long> optionIds) {
        // 1. 투표가 유효한지 체크
        Poll poll = pollRepository.findById(pollId).orElseThrow(() -> new PollNotFoundException(POLL_NOT_FOUND));

        // 2. 투표 상태 확인
        if (poll.getState() != PollState.PUBLISHED) {
            throw new PollForbiddenException(POLL_NOT_PUBLISHED);
        }

        // 3. 투표 기간 확인
        LocalDateTime now = LocalDateTime.now();
        if (poll.getStartAt().isAfter(now) || poll.getEndAt().isBefore(now)) {
            throw new PollForbiddenException(POLL_NOT_IN_VOTING_PERIOD);
        }

        // 4. 선택된 옵션들이 해당 투표에 속하는지 확인
        List<PollOption> options = pollOptionRepository.findAllById(optionIds);
        if (options.size() != optionIds.size()) {
            throw new PollNotFoundException(POLL_OR_OPTION_NOT_FOUND);
        }

        boolean allMatch = options.stream().allMatch(option -> option.getPoll().getId().equals(pollId));
        if (!allMatch) {
            throw new PollNotFoundException(POLL_OR_OPTION_NOT_FOUND);
        }

        // 5. 선택된 옵션 수가 min_choices ~ max_choices 범위에 포함되는지 확인
        if (optionIds.size() < poll.getMinChoices() || optionIds.size() > poll.getMaxChoices()) {
            throw new PollBadRequestException(POLL_INVALID_SELECTION_COUNT);
        }

        // 6. 이미 투표한 사용자인지 확인
        List<Vote> existingVotes = voteRepository.findByUserIdAndPollId(user.getId(), pollId);
        if (!existingVotes.isEmpty()) {
            throw new PollConflictException(POLL_ALREADY_VOTED);
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

        // 8. 투표 통계 업데이트
        // TODO: 추가적으로 투표 통계 테이블에 반영하는 로직이 필요하다면 여기서 업데이트
    }
}
