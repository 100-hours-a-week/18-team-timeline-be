package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.domain.*;
import com.tamnara.backend.poll.dto.*;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;

    @Transactional
    public Long createPoll(PollCreateRequest request) {
        // 1. 유효성 추가 검증
        if (request.getMinChoices() > request.getMaxChoices()) {
            throw new IllegalArgumentException("최소 선택 수는 최대 선택 수보다 클 수 없습니다.");
        }
        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new IllegalArgumentException("시작일은 종료일보다 앞서야 합니다.");
        }

        // 2. Poll 저장
        Poll poll = Poll.builder()
                .title(request.getTitle())
                .minChoices(request.getMinChoices())
                .maxChoices(request.getMaxChoices())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .state(PollState.DRAFT)
                .build();
        Poll savedPoll = pollRepository.save(poll);

        // 3. 옵션 저장
        List<PollOption> options = new ArrayList<>();
        int sortOrder = 0;
        for (PollOptionCreateRequest optionReq : request.getOptions()) {
            PollOption option = PollOption.builder()
                    .title(optionReq.getTitle())
                    .imageUrl(optionReq.getImageUrl())
                    .sortOrder(sortOrder++)
                    .poll(savedPoll)
                    .build();
            options.add(option);
        }
        pollOptionRepository.saveAll(options);

        return savedPoll.getId();
    }
}
