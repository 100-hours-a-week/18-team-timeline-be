package com.tamnara.backend.poll.controller;

import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.poll.dto.PollCreateRequest;
import com.tamnara.backend.poll.dto.PollCreateResponse;
import com.tamnara.backend.poll.service.PollService;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import static com.tamnara.backend.global.constant.ResponseMessage.*;
import static com.tamnara.backend.poll.constant.PollResponseMessage.*;

@RestController
@RequestMapping("/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WrappedDTO<PollCreateResponse>> createPoll(
            @Valid @RequestBody PollCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new WrappedDTO<>(false, BAD_REQUEST, null)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new WrappedDTO<>(false, INTERNAL_SERVER_ERROR, null)
            );
        }
    }
}
