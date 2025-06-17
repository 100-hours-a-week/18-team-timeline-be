package com.tamnara.backend.bookmark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.bookmark.config.BookmarkServiceMockConfig;
import com.tamnara.backend.bookmark.constant.BookmarkResponseMessage;
import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;
import com.tamnara.backend.bookmark.service.BookmarkService;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BookmarkServiceMockConfig.class)
@ActiveProfiles("test")
public class BookmarkControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BookmarkService bookmarkService;

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

    @Test
    void 로그아웃_상태에서_북마크_설정_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        mockMvc.perform(post("/news/{newsId}/bookmark", NEWS_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseMessage.USER_NOT_CERTIFICATION));
    }

    @Test
    void 로그인_상태에서_북마크_설정_검증() throws Exception {
        // given
        Long bookmarkId = 1L;
        BookmarkAddResponse bookmarkAddResponse = new BookmarkAddResponse(bookmarkId);
        given(bookmarkService.save(USER_ID, NEWS_ID)).willReturn(bookmarkAddResponse);

        // when & then
        mockMvc.perform(post("/news/{newsId}/bookmark", NEWS_ID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(BookmarkResponseMessage.BOOKMARK_ADDED_SUCCESS))
                .andExpect(jsonPath("$.data.bookmarkId").value(bookmarkId));
    }

    @Test
    void 로그아웃_상태에서_북마크_해제_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        mockMvc.perform(delete("/news/{newsId}/bookmark", NEWS_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseMessage.USER_NOT_CERTIFICATION));
    }

    @Test
    void 로그인_상태에서_북마크_해제_검증() throws Exception {
        // given

        // when & then
        mockMvc.perform(delete("/news/{newsId}/bookmark", NEWS_ID))
                .andExpect(status().isNoContent());
    }
}
