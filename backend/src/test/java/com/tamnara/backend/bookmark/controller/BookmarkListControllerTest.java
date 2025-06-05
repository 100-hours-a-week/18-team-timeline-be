package com.tamnara.backend.bookmark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.bookmark.config.BookmarkServiceMockConfig;
import com.tamnara.backend.bookmark.constant.BookmarkResponseMessage;
import com.tamnara.backend.bookmark.constant.BookmarkServiceConstant;
import com.tamnara.backend.bookmark.dto.response.BookmarkListResponse;
import com.tamnara.backend.bookmark.service.BookmarkService;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.news.dto.NewsCardDTO;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookmarkListController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BookmarkServiceMockConfig.class)
@ActiveProfiles("test")
public class BookmarkListControllerTest {

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

    private NewsCardDTO createNewsCardDTO(Long newsId) {
        return new NewsCardDTO(
                newsId,
                "제목",
                "미리보기 내용",
                "url",
                null,
                LocalDateTime.now(),
                true,
                LocalDateTime.now()
        );
    }

    @Test
    void 로그아웃_상태에서_북마크_목록_조회_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        mockMvc.perform(
                get("/users/me/bookmarks")
                        .param("offset", "0")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseMessage.USER_NOT_CERTIFICATION));
    }

    @Test
    void 로그인_상태에서_북마크_목록_최초_조회_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L);

        BookmarkListResponse bookmarkListResponse = new BookmarkListResponse(
                List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3),
                BookmarkServiceConstant.PAGE_SIZE,
                true
        );

        given(bookmarkService.getBookmarkedNewsList(USER_ID, 0)).willReturn(bookmarkListResponse);

        // when & then
        mockMvc.perform(
                get("/users/me/bookmarks")
                        .param("offset", "0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(BookmarkResponseMessage.BOOKMARKED_NEWS_LIST_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.bookmarks.length()").value(3))
                .andExpect(jsonPath("$.data.offset").value(bookmarkListResponse.getOffset()))
                .andExpect(jsonPath("$.data.hasNext").value(true));
    }

    @Test
    void 로그인_상태에서_북마크_목록_추가_조회_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L);

        BookmarkListResponse bookmarkListResponse = new BookmarkListResponse(
                List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3),
                BookmarkServiceConstant.PAGE_SIZE,
                false
        );

        given(bookmarkService.getBookmarkedNewsList(USER_ID, BookmarkServiceConstant.PAGE_SIZE)).willReturn(bookmarkListResponse);

        // when & then
        mockMvc.perform(
                        get("/users/me/bookmarks")
                                .param("offset", String.valueOf(BookmarkServiceConstant.PAGE_SIZE))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(BookmarkResponseMessage.BOOKMARKED_NEWS_LIST_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.bookmarks.length()").value(3))
                .andExpect(jsonPath("$.data.offset").value(bookmarkListResponse.getOffset()))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }
}
