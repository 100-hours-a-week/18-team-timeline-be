package com.tamnara.backend.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.comment.config.CommentServiceMockConfig;
import com.tamnara.backend.comment.constant.CommentResponseMessage;
import com.tamnara.backend.comment.constant.CommentServiceConstant;
import com.tamnara.backend.comment.dto.CommentDTO;
import com.tamnara.backend.comment.dto.request.CommentCreateRequest;
import com.tamnara.backend.comment.dto.response.CommentListResponse;
import com.tamnara.backend.comment.service.CommentService;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CommentServiceMockConfig.class)
@ActiveProfiles("test")
public class CommentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CommentService commentService;

    private static final Long USER_ID = 1L;
    private static final Long NEWS_ID = 1L;

    @BeforeEach
    void setupSecurityContext() {
        User user = User.builder()
                .id(USER_ID)
                .username("테스트유저")
                .role(Role.USER)
                .build();

        UserDetailsImpl principal = new UserDetailsImpl(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private CommentDTO createCommentDTO(Long id, LocalDateTime createdAt) {
        return new CommentDTO(
                id,
                USER_ID,
                "댓글 내용",
                createdAt
        );
    }

    @Test
    void 로그아웃_상태에서_댓글_목록_최초_조회_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        Integer offset = 0;

        CommentDTO commentDTO1 = createCommentDTO(1L, LocalDateTime.now().minusDays(2));
        CommentDTO commentDTO2 = createCommentDTO(2L, LocalDateTime.now().minusDays(1));
        CommentDTO commentDTO3 = createCommentDTO(3L, LocalDateTime.now());

        CommentListResponse commentListResponse = new CommentListResponse(
                List.of(commentDTO1, commentDTO2, commentDTO3),
                CommentServiceConstant.PAGE_SIZE,
                false
        );
        given(commentService.getComments(NEWS_ID, offset)).willReturn(commentListResponse);

        // when & then
        mockMvc.perform(
                get("/news/{newsId}/comments", NEWS_ID)
                        .param("offset", String.valueOf(offset))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(CommentResponseMessage.COMMENT_LIST_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.comments.length()").value(3));
    }

    @Test
    void 로그인_상태에서_댓글_목록_최초_조회_검증() throws Exception {
        // given
        Integer offset = 0;

        CommentDTO commentDTO1 = createCommentDTO(1L, LocalDateTime.now().minusDays(2));
        CommentDTO commentDTO2 = createCommentDTO(2L, LocalDateTime.now().minusDays(1));
        CommentDTO commentDTO3 = createCommentDTO(3L, LocalDateTime.now());

        CommentListResponse commentListResponse = new CommentListResponse(
                List.of(commentDTO1, commentDTO2, commentDTO3),
                CommentServiceConstant.PAGE_SIZE,
                false
        );
        given(commentService.getComments(NEWS_ID, offset)).willReturn(commentListResponse);

        // when & then
        mockMvc.perform(
                get("/news/{newsId}/comments", NEWS_ID)
                        .param("offset", String.valueOf(offset))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(CommentResponseMessage.COMMENT_LIST_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.comments.length()").value(3));
    }

    @Test
    void 로그아웃_상태에서_댓글_목록_추가_조회_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        Integer offset = 20;

        CommentDTO commentDTO1 = createCommentDTO(1L, LocalDateTime.now().minusDays(2));
        CommentDTO commentDTO2 = createCommentDTO(2L, LocalDateTime.now().minusDays(1));
        CommentDTO commentDTO3 = createCommentDTO(3L, LocalDateTime.now());

        CommentListResponse commentListResponse = new CommentListResponse(
                List.of(commentDTO1, commentDTO2, commentDTO3),
                CommentServiceConstant.PAGE_SIZE,
                false
        );
        given(commentService.getComments(NEWS_ID, offset)).willReturn(commentListResponse);

        // when & then
        mockMvc.perform(
                get("/news/{newsId}/comments", NEWS_ID)
                        .param("offset", String.valueOf(offset))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(CommentResponseMessage.COMMENT_LIST_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.comments.length()").value(3));
    }

    @Test
    void 로그인_상태에서_댓글_목록_추가_조회_검증() throws Exception {
        // given
        Integer offset = 20;

        CommentDTO commentDTO1 = createCommentDTO(1L, LocalDateTime.now().minusDays(2));
        CommentDTO commentDTO2 = createCommentDTO(2L, LocalDateTime.now().minusDays(1));
        CommentDTO commentDTO3 = createCommentDTO(3L, LocalDateTime.now());

        CommentListResponse commentListResponse = new CommentListResponse(
                List.of(commentDTO1, commentDTO2, commentDTO3),
                CommentServiceConstant.PAGE_SIZE,
                false
        );
        given(commentService.getComments(NEWS_ID, offset)).willReturn(commentListResponse);

        // when & then
        mockMvc.perform(
                get("/news/{newsId}/comments", NEWS_ID)
                        .param("offset", String.valueOf(offset))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(CommentResponseMessage.COMMENT_LIST_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.comments.length()").value(3));
    }

    @Test
    void 로그아웃_상태에서_댓글_저장_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        Long commentId = 1L;
        CommentCreateRequest request = new CommentCreateRequest("댓글 내용");
        given(commentService.save(anyLong(), anyLong(), any(CommentCreateRequest.class))).willReturn(commentId);

        // when & then
        mockMvc.perform(
                    post("/news/{newsId}/comments", NEWS_ID)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseMessage.USER_NOT_CERTIFICATION));
    }

    @Test
    void 로그인_상태에서_댓글_저장_검증() throws Exception {
        // given
        Long commentId = 1L;
        CommentCreateRequest request = new CommentCreateRequest("댓글 내용");
        given(commentService.save(anyLong(), anyLong(), any(CommentCreateRequest.class))).willReturn(commentId);

        // when & then
        mockMvc.perform(
                        post("/news/{newsId}/comments", NEWS_ID)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(CommentResponseMessage.COMMENT_CREATED_SUCCESS))
                .andExpect(jsonPath("$.data.commentId").value(commentId));
    }

    @Test
    void 로그아웃_상태에서_댓글_삭제_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        Long commentId = 1L;

        // when & then
        mockMvc.perform(delete("/news/{newsId}/comments/{commentId}", NEWS_ID, commentId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseMessage.USER_NOT_CERTIFICATION));
    }

    @Test
    void 로그인_상태에서_댓글_삭제_검증() throws Exception {
        // given
        Long commentId = 1L;

        // when & then
        mockMvc.perform(delete("/news/{newsId}/comments/{commentId}", NEWS_ID, commentId))
                .andExpect(status().isNoContent());
    }

}
