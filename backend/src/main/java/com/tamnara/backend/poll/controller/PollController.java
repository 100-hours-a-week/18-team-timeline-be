package com.tamnara.backend.poll.controller;

import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.poll.dto.PollCreateRequest;
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

import java.util.HashMap;
import java.util.Map;

import static com.tamnara.backend.poll.constant.PollResponseMessage.*;

@RestController
@RequestMapping("/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WrappedDTO<Long>> createPoll(
            @Valid @RequestBody PollCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userDetails.getUser();

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new WrappedDTO<>(false, "관련된 회원이 없습니다.", null)
                );
            }

            if (user.getState() != State.ACTIVE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new WrappedDTO<>(false, "유효하지 않은 계정입니다.", null)
                );
            }

            Long pollId = pollService.createPoll(request);

            Map<String, Long> responseData = new HashMap<>();
            responseData.put("pollId", pollId);

            return ResponseEntity.ok(new WrappedDTO<>(true, "투표 생성 성공", responseData));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                new WrappedDTO<>(false, e.getMessage(), null)
        );
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(
                new WrappedDTO<>(false, "서버 내부 오류가 발생했습니다. 나중에 다시 시도해주세요.", null)
        );
    }
}
