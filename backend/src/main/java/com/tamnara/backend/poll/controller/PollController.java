package com.tamnara.backend.poll.controller;

import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.dto.*;
import com.tamnara.backend.poll.service.PollService;
import com.tamnara.backend.poll.service.VoteService;
import com.tamnara.backend.poll.service.VoteStatisticsService;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import static com.tamnara.backend.global.constant.ResponseMessage.*;
import static com.tamnara.backend.poll.constant.PollResponseMessage.*;

@Slf4j
@RestController
@RequestMapping("/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;
    private final VoteService voteService;
    private final VoteStatisticsService voteStatisticsService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WrappedDTO<PollCreateResponse>> createPoll(
        @Valid @RequestBody PollCreateRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new WrappedDTO<>(false, USER_NOT_FOUND, null)
            );
        }

        if (user.getState() != State.ACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new WrappedDTO<>(false, ACCOUNT_FORBIDDEN, null)
            );
        }

        Long pollId = pollService.createPoll(request);
        PollCreateResponse response = new PollCreateResponse(pollId);

        return ResponseEntity.ok(new WrappedDTO<>(true, POLL_CREATED, response));
    }

    @GetMapping("/{pollId}")
    public ResponseEntity<WrappedDTO<PollInfoResponse>> getPollInfo(
            @PathVariable Long pollId) {

        Poll poll = pollService.getPollById(pollId);

        if (poll == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new WrappedDTO<>(false, POLL_NOT_FOUND, null)
            );
        }

        if (poll.getState() == PollState.DRAFT || poll.getState() == PollState.SCHEDULED || poll.getState() == PollState.DELETED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new WrappedDTO<>(false, POLL_NOT_PUBLISHED, null)
            );
        }

        PollInfoResponse pollInfoResponse = new PollInfoResponse(poll);

        return ResponseEntity.ok(new WrappedDTO<>(true, POLL_OK, pollInfoResponse));
    }

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<WrappedDTO<Void>> vote(
            @PathVariable Long pollId,
            @Valid @RequestBody VoteRequest pollVoteRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new WrappedDTO<>(false, USER_NOT_FOUND, null)
            );
        }

        if (user.getState() != State.ACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new WrappedDTO<>(false, ACCOUNT_FORBIDDEN, null)
            );
        }

        voteService.vote(pollId, user, pollVoteRequest.getOptionIds());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new WrappedDTO<>(true, VOTE_SUCCESS, null)
        );
    }


    @GetMapping("/{pollId}/stats")
    public ResponseEntity<WrappedDTO<PollStatisticsResponse>> getPollStatistics(
            @PathVariable Long pollId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("userDetails: {}", userDetails);
        User user = userDetails.getUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new WrappedDTO<>(false, USER_NOT_FOUND, null)
            );
        }

        if (user.getState() != State.ACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new WrappedDTO<>(false, ACCOUNT_FORBIDDEN, null)
            );
        }

        PollStatisticsResponse response = voteStatisticsService.getPollStatistics(pollId);

        return ResponseEntity.ok(new WrappedDTO<>(true, POLL_OK, response));
    }
}
