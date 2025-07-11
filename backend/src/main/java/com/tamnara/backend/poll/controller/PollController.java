package com.tamnara.backend.poll.controller;

import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.poll.constant.PollResponseMessage;
import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.request.VoteRequest;
import com.tamnara.backend.poll.dto.response.PollIdResponse;
import com.tamnara.backend.poll.dto.response.PollInfoResponse;
import com.tamnara.backend.poll.dto.response.PollStatisticsResponse;
import com.tamnara.backend.poll.service.PollService;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

import static com.tamnara.backend.global.constant.ResponseMessage.USER_FORBIDDEN;
import static com.tamnara.backend.global.constant.ResponseMessage.USER_NOT_FOUND;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_CREATED;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_OK;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_SCHEDULED;
import static com.tamnara.backend.poll.constant.PollResponseMessage.VOTE_SUCCESS;

@Slf4j
@RestController
@RequestMapping("/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WrappedDTO<PollIdResponse>> createPoll(
        @Valid @RequestBody PollCreateRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails.getUser() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new WrappedDTO<>(false, USER_NOT_FOUND, null)
            );
        }

        if (userDetails.getUser().getState() != State.ACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new WrappedDTO<>(false, USER_FORBIDDEN, null)
            );
        }

        Long pollId = pollService.createPoll(request);
        PollIdResponse response = new PollIdResponse(pollId);

        URI location = URI.create("/polls");
        return ResponseEntity
                .created(location)
                .body(new WrappedDTO<>(true, POLL_CREATED, response));

    }

    @GetMapping()
    public ResponseEntity<WrappedDTO<PollInfoResponse>> getPollInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            return ResponseEntity.ok(new WrappedDTO<>(
                    true,
                    POLL_OK,
                    pollService.getLatestPublishedPoll(userDetails.getUser().getId())
            ));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/vote")
    public ResponseEntity<WrappedDTO<PollIdResponse>> vote(
            @Valid @RequestBody VoteRequest voteRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails.getUser() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new WrappedDTO<>(false, USER_NOT_FOUND, null)
                );
            }

            if (userDetails.getUser().getState() != State.ACTIVE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new WrappedDTO<>(false, USER_FORBIDDEN, null)
                );
            }

            PollIdResponse response = pollService.vote(userDetails.getUser(), voteRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new WrappedDTO<>(
                            true,
                            VOTE_SUCCESS,
                            response
                    ));
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pollId}/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WrappedDTO<Void>> schedule(
            @PathVariable Long pollId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            if (userDetails.getUser() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new WrappedDTO<>(false, USER_NOT_FOUND, null)
                );
            }

            if (userDetails.getUser().getState() != State.ACTIVE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new WrappedDTO<>(false, USER_FORBIDDEN, null)
                );
            }

            pollService.schedulePoll(pollService.getPollById(pollId).getId());

            return ResponseEntity.ok(new WrappedDTO<>(true, POLL_SCHEDULED, null));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pollId}/stats")
    @Operation(
            summary = "투표 결과 통계 조회",
            description = "특정 투표의 전체 투표수 및 각 선택지별 투표수를 조회합니다. 선택지 목록은 투표수의 내림차순으로 정렬됩니다."
    )
    public ResponseEntity<WrappedDTO<PollStatisticsResponse>> getVoteStatistics(
            @PathVariable Long pollId
    ) {
        try {
            return ResponseEntity.ok(new WrappedDTO<>(
                    true,
                    PollResponseMessage.VOTE_STATISTICS,
                    pollService.getVoteStatistics(pollId)
            ));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
