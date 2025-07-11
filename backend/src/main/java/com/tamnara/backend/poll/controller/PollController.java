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
import org.springframework.web.bind.annotation.*;
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
    @Operation(
            summary = "투표 생성",
            description = "새로운 투표를 대기 상태로 생성한다. 공개하기 위해선 공개 예정 설정이 추가로 필요하다."
    )
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
    @Operation(
            summary = "최신 공개 투표 조회",
            description = "최신 공개 상태의 투표를 조회한다."
    )
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
    @Operation(
            summary = "투표 선택 제출",
            description = "투표의 선택지를 1개 이상 선택하여 제출한다."
    )
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

    @PatchMapping("/{pollId}/state")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "투표 공개 예정 설정",
            description = "대기 상태로 저장된 투표의 상태를 공개 예정으로 변경한다. 공개 예정 상태의 투표는 월요일 오전 10시에 공개된다."
    )
    public ResponseEntity<WrappedDTO<Void>> schedule(
            @PathVariable Long pollId,
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
