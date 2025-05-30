package com.tamnara.backend.news.controller;

import com.tamnara.backend.config.NewsServiceMockConfig;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.dto.response.HotissueNewsListResponse;
import com.tamnara.backend.news.service.NewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NewsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(NewsServiceMockConfig.class)
@ActiveProfiles("test")
public class NewsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private NewsService newsService;

    private static final Integer PAGE_SIZE = 20;

    private String createFakeJwtToken() {
        return "test.jwt.token"; // 임시로 'Authorization' 헤더에 넣을 가짜 토큰
    }

    private NewsCardDTO createNewsCardDTO(Long id, String category, LocalDateTime updatedAt, boolean bookmarked) {
        return new NewsCardDTO(
                id,
                "제목",
                "미리보기 내용",
                "이미지 링크",
                category,
                updatedAt,
                bookmarked,
                bookmarked ? LocalDateTime.now() : null
        );
    };

    @Test
    void 로그아웃_상태에서_핫이슈_뉴스_카드_목록_조회_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);

        HotissueNewsListResponse mockResponse = new HotissueNewsListResponse(newsList);
        given(newsService.getHotissueNewsCardPage()).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/news/hotissue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("요청하신 핫이슈 뉴스 카드 목록을 성공적으로 불러왔습니다."))
                .andExpect(jsonPath("$.data.newsList.length()").value(3))
                .andExpect(jsonPath("$.data.newsList[0].id").value(1))
                .andExpect(jsonPath("$.data.newsList[1].id").value(2))
                .andExpect(jsonPath("$.data.newsList[2].id").value(3));
    }

    @Test
    @WithMockUser
    void 로그인_상태에서_핫이슈_뉴스_카드_목록_조회_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), true);
        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);

        HotissueNewsListResponse mockResponse = new HotissueNewsListResponse(newsList);
        given(newsService.getHotissueNewsCardPage()).willReturn(mockResponse);

        // when & then
        mockMvc.perform(
                get("/news/hotissue")
                        .header("Authorization", "Bearer " + createFakeJwtToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("요청하신 핫이슈 뉴스 카드 목록을 성공적으로 불러왔습니다."))
                .andExpect(jsonPath("$.data.newsList.length()").value(3))
                .andExpect(jsonPath("$.data.newsList[0].id").value(1))
                .andExpect(jsonPath("$.data.newsList[1].id").value(2))
                .andExpect(jsonPath("$.data.newsList[2].id").value(3));
    }
}
