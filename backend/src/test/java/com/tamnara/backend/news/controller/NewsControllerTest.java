package com.tamnara.backend.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.news.config.NewsServiceMockConfig;
import com.tamnara.backend.news.constant.NewsResponseMessage;
import com.tamnara.backend.news.constant.NewsServiceConstant;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.TimelineCardType;
import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.dto.NewsDetailDTO;
import com.tamnara.backend.news.dto.StatisticsDTO;
import com.tamnara.backend.news.dto.TimelineCardDTO;
import com.tamnara.backend.news.dto.request.KtbNewsCreateRequest;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.HotissueNewsListResponse;
import com.tamnara.backend.news.dto.response.NewsListResponse;
import com.tamnara.backend.news.dto.response.category.AllResponse;
import com.tamnara.backend.news.dto.response.category.EconomyResponse;
import com.tamnara.backend.news.dto.response.category.EntertainmentResponse;
import com.tamnara.backend.news.dto.response.category.KtbResponse;
import com.tamnara.backend.news.dto.response.category.MultiCategoryResponse;
import com.tamnara.backend.news.dto.response.category.SportsResponse;
import com.tamnara.backend.news.service.NewsService;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NewsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(NewsServiceMockConfig.class)
@ActiveProfiles("test")
public class NewsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private NewsService newsService;

    private static final Long USER_ID = 1L;

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
    }

    private TimelineCardDTO createTimelineCardDTO() {
        return new TimelineCardDTO(
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.DAY.toString(),
                LocalDate.now(),
                LocalDate.now()
        );
    }

    private NewsDetailDTO createNewsDetailDTO(Long id, boolean bookmarked) {
        TimelineCardDTO timelineCardDTO1 = createTimelineCardDTO();
        TimelineCardDTO timelineCardDTO2 = createTimelineCardDTO();

        return new NewsDetailDTO(
                id,
                "제목",
                "이미지 링크",
                CategoryType.SPORTS.name(),
                LocalDateTime.now(),
                bookmarked,
                List.of(timelineCardDTO1, timelineCardDTO2),
                new StatisticsDTO(50, 30, 20)
        );
    }

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
    void 로그아웃_상태에서_핫이슈_뉴스_카드_목록_조회_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

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
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.HOTISSUE_NEWS_CARD_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.newsList.length()").value(3))
                .andExpect(jsonPath("$.data.newsList[0].id").value(1))
                .andExpect(jsonPath("$.data.newsList[1].id").value(2))
                .andExpect(jsonPath("$.data.newsList[2].id").value(3));
    }

    @Test
    void 로그인_상태에서_핫이슈_뉴스_카드_목록_조회_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), true);
        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);

        HotissueNewsListResponse mockResponse = new HotissueNewsListResponse(newsList);
        given(newsService.getHotissueNewsCardPage()).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/news/hotissue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.HOTISSUE_NEWS_CARD_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.newsList.length()").value(3))
                .andExpect(jsonPath("$.data.newsList[0].id").value(1))
                .andExpect(jsonPath("$.data.newsList[1].id").value(2))
                .andExpect(jsonPath("$.data.newsList[2].id").value(3));
    }

    @Test
    void 로그아웃_상태에서_일반_뉴스_카드_목록_최초_로딩_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.SPORTS.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO4 = createNewsCardDTO(4L, CategoryType.KTB.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO5 = createNewsCardDTO(5L, null, LocalDateTime.now(), false);

        List<NewsCardDTO> allnewsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3, newsCardDTO4, newsCardDTO5);
        NewsListResponse allResponse = new NewsListResponse(allnewsList, NewsServiceConstant.PAGE_SIZE, false);

        List<NewsCardDTO> economynewsList = List.of(newsCardDTO1);
        NewsListResponse economyResponse = new NewsListResponse(economynewsList, NewsServiceConstant.PAGE_SIZE, false);

        List<NewsCardDTO> entertainmentnewsList = List.of(newsCardDTO2);
        NewsListResponse entertainmentResponse = new NewsListResponse(entertainmentnewsList, NewsServiceConstant.PAGE_SIZE, false);

        List<NewsCardDTO> sportsnewsList = List.of(newsCardDTO3);
        NewsListResponse sportsResponse = new NewsListResponse(sportsnewsList, NewsServiceConstant.PAGE_SIZE, false);

        List<NewsCardDTO> ktbnewsList = List.of(newsCardDTO4);
        NewsListResponse ktbResponse = new NewsListResponse(ktbnewsList, NewsServiceConstant.PAGE_SIZE, false);

        MultiCategoryResponse response = new MultiCategoryResponse();
        response.setAll(allResponse);
        response.setEconomy(economyResponse);
        response.setEntertainment(entertainmentResponse);
        response.setSports(sportsResponse);
        response.setKtb(ktbResponse);

        given(newsService.getMultiCategoryPage(null, 0)).willReturn(response);

        // when & then
        mockMvc.perform(
                get("/news")
                        .param("offset", "0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.ALL.newsList.size()").value(5))
                .andExpect(jsonPath("$.data.ECONOMY.newsList.size()").value(1))
                .andExpect(jsonPath("$.data.ECONOMY.newsList[0].category").value(CategoryType.ECONOMY.toString()))
                .andExpect(jsonPath("$.data.ENTERTAINMENT.newsList.size()").value(1))
                .andExpect(jsonPath("$.data.ENTERTAINMENT.newsList[0].category").value(CategoryType.ENTERTAINMENT.toString()))
                .andExpect(jsonPath("$.data.SPORTS.newsList.size()").value(1))
                .andExpect(jsonPath("$.data.SPORTS.newsList[0].category").value(CategoryType.SPORTS.toString()))
                .andExpect(jsonPath("$.data.KTB.newsList.size()").value(1))
                .andExpect(jsonPath("$.data.KTB.newsList[0].category").value(CategoryType.KTB.toString()));
    }

    @Test
    void 로그인_상태에서_일반_뉴스_카드_목록_최초_로딩_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.SPORTS.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO4 = createNewsCardDTO(4L, CategoryType.KTB.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO5 = createNewsCardDTO(5L, null, LocalDateTime.now(), true);

        List<NewsCardDTO> allNewsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3, newsCardDTO4, newsCardDTO5);
        NewsListResponse allResponse = new NewsListResponse(allNewsList, NewsServiceConstant.PAGE_SIZE, false);

        List<NewsCardDTO> economyNewsList = List.of(newsCardDTO1);
        NewsListResponse economyResponse = new NewsListResponse(economyNewsList, NewsServiceConstant.PAGE_SIZE, false);

        List<NewsCardDTO> entertainmentNewsList = List.of(newsCardDTO2);
        NewsListResponse entertainmentResponse = new NewsListResponse(entertainmentNewsList, NewsServiceConstant.PAGE_SIZE, false);

        List<NewsCardDTO> sportsNewsList = List.of(newsCardDTO3);
        NewsListResponse sportsResponse = new NewsListResponse(sportsNewsList, NewsServiceConstant.PAGE_SIZE, false);

        List<NewsCardDTO> ktbNewsList = List.of(newsCardDTO4);
        NewsListResponse ktbResponse = new NewsListResponse(ktbNewsList, NewsServiceConstant.PAGE_SIZE, false);

        MultiCategoryResponse response = new MultiCategoryResponse();
        response.setAll(allResponse);
        response.setEconomy(economyResponse);
        response.setEntertainment(entertainmentResponse);
        response.setSports(sportsResponse);
        response.setKtb(ktbResponse);

        given(newsService.getMultiCategoryPage(USER_ID, 0)).willReturn(response);

        // when & then
        mockMvc.perform(
                    get("/news")
                            .param("offset", "0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.ALL.newsList.size()").value(5))
                .andExpect(jsonPath("$.data.ECONOMY.newsList.size()").value(1))
                .andExpect(jsonPath("$.data.ECONOMY.newsList[0].category").value(CategoryType.ECONOMY.toString()))
                .andExpect(jsonPath("$.data.ENTERTAINMENT.newsList.size()").value(1))
                .andExpect(jsonPath("$.data.ENTERTAINMENT.newsList[0].category").value(CategoryType.ENTERTAINMENT.toString()))
                .andExpect(jsonPath("$.data.SPORTS.newsList.size()").value(1))
                .andExpect(jsonPath("$.data.SPORTS.newsList[0].category").value(CategoryType.SPORTS.toString()))
                .andExpect(jsonPath("$.data.KTB.newsList.size()").value(1))
                .andExpect(jsonPath("$.data.KTB.newsList[0].category").value(CategoryType.KTB.toString()));
    }

    @Test
    void 로그아웃_상태에서_전체_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.SPORTS.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO4 = createNewsCardDTO(4L, CategoryType.KTB.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO5 = createNewsCardDTO(5L, null, LocalDateTime.now(), false);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3, newsCardDTO4, newsCardDTO5);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        AllResponse response = new AllResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(null, null, NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                    get("/news")
                            .param("category", (String) null)
                            .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.ALL.newsList.size()").value(5))
                .andExpect(jsonPath("$.data.ALL.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.ALL.hasNext").value(false));
    }

    @Test
    void 로그인_상태에서_전체_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.SPORTS.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO4 = createNewsCardDTO(4L, CategoryType.KTB.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO5 = createNewsCardDTO(5L, null, LocalDateTime.now(), true);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3, newsCardDTO4, newsCardDTO5);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        AllResponse response = new AllResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(USER_ID, null, NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                    get("/news")
                            .param("category", (String) null)
                            .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.ALL.newsList.size()").value(5))
                .andExpect(jsonPath("$.data.ALL.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.ALL.hasNext").value(false));
    }

    @Test
    void 로그아웃_상태에서_경제_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        EconomyResponse response = new EconomyResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(null, CategoryType.ECONOMY.toString(), NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                    get("/news")
                            .param("category", CategoryType.ECONOMY.toString())
                            .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.ECONOMY.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.ECONOMY.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.ECONOMY.hasNext").value(false));
    }

    @Test
    void 로그인_상태에서_경제_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ECONOMY.toString(), LocalDateTime.now(), true);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        EconomyResponse response = new EconomyResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(USER_ID, CategoryType.ECONOMY.toString(), NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                    get("/news")
                            .param("category", CategoryType.ECONOMY.toString())
                            .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.ECONOMY.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.ECONOMY.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.ECONOMY.hasNext").value(false));
    }

    @Test
    void 로그아웃_상태에서_연예_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        EntertainmentResponse response = new EntertainmentResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(null, CategoryType.ENTERTAINMENT.toString(), NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                    get("/news")
                            .param("category", CategoryType.ENTERTAINMENT.toString())
                            .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.ENTERTAINMENT.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.ENTERTAINMENT.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.ENTERTAINMENT.hasNext").value(false));
    }

    @Test
    void 로그인_상태에서_연예_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), true);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        EntertainmentResponse response = new EntertainmentResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(USER_ID, CategoryType.ENTERTAINMENT.toString(), NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                    get("/news")
                            .param("category", CategoryType.ENTERTAINMENT.toString())
                            .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.ENTERTAINMENT.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.ENTERTAINMENT.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.ENTERTAINMENT.hasNext").value(false));
    }

    @Test
    void 로그아웃_상태에서_스포츠_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.SPORTS.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.SPORTS.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.SPORTS.toString(), LocalDateTime.now(), false);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        SportsResponse response = new SportsResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(null, CategoryType.SPORTS.toString(), NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                get("/news")
                        .param("category", CategoryType.SPORTS.toString())
                        .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.SPORTS.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.SPORTS.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.SPORTS.hasNext").value(false));
    }

    @Test
    void 로그인_상태에서_스포츠_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.SPORTS.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.SPORTS.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.SPORTS.toString(), LocalDateTime.now(), true);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        SportsResponse response = new SportsResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(USER_ID, CategoryType.SPORTS.toString(), NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                get("/news")
                        .param("category", CategoryType.SPORTS.toString())
                        .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.SPORTS.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.SPORTS.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.SPORTS.hasNext").value(false));
    }

    @Test
    void 로그아웃_상태에서_카테부_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.KTB.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.KTB.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.KTB.toString(), LocalDateTime.now(), false);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        KtbResponse response = new KtbResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(null, CategoryType.KTB.toString(), NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                get("/news")
                        .param("category", CategoryType.KTB.toString())
                        .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.KTB.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.KTB.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.KTB.hasNext").value(false));
    }

    @Test
    void 로그인_상태에서_카테부_카테고리의_일반_뉴스_카드_목록_추가_로딩_검증() throws Exception {
        // given
        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.KTB.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.KTB.name(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.KTB.toString(), LocalDateTime.now(), true);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse newsListResponse = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        KtbResponse response = new KtbResponse(newsListResponse);
        given(newsService.getSingleCategoryPage(USER_ID, CategoryType.KTB.toString(), NewsServiceConstant.PAGE_SIZE * 2)).willReturn(response);

        // when & then
        mockMvc.perform(
                get("/news")
                        .param("category", CategoryType.KTB.toString())
                        .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE * 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.KTB.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.KTB.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.KTB.hasNext").value(false));
    }

    @Test
    void 로그아웃_상태에서_뉴스_검색_결과_최초_조회_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        List<String> tags = List.of("태그1", "태그2", "태그3");

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.KTB.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ECONOMY.name(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse response = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE, false);
        given(newsService.getSearchNewsCardPage(null, tags, 0)).willReturn(response);

        // when & then
        mockMvc.perform(
                get("/news/search")
                        .param("tags", tags.toArray(new String[0]))
                        .param("offset", String.valueOf(0))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.SEARCHED_NEWS_CARD_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.offset").value(NewsServiceConstant.PAGE_SIZE))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void 로그인_상태에서_뉴스_검색_결과_최초_조회_검증() throws Exception {
        // given
        List<String> tags = List.of("태그1", "태그2", "태그3");

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.KTB.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ECONOMY.name(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), true);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse response = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE, false);
        given(newsService.getSearchNewsCardPage(USER_ID, tags, 0)).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/news/search")
                                .param("tags", tags.toArray(new String[0]))
                                .param("offset", String.valueOf(0))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.SEARCHED_NEWS_CARD_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.offset").value(NewsServiceConstant.PAGE_SIZE))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void 로그아웃_상태에서_뉴스_검색_결과_추가_조회_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        List<String> tags = List.of("태그1", "태그2", "태그3");

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.KTB.toString(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ECONOMY.name(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), false);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse response = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        given(newsService.getSearchNewsCardPage(null, tags, NewsServiceConstant.PAGE_SIZE)).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/news/search")
                                .param("tags", tags.toArray(new String[0]))
                                .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.SEARCHED_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void 로그인_상태에서_뉴스_검색_결과_추가_조회_검증() throws Exception {
        // given
        List<String> tags = List.of("태그1", "태그2", "태그3");

        NewsCardDTO newsCardDTO1 = createNewsCardDTO(1L, CategoryType.KTB.toString(), LocalDateTime.now(), true);
        NewsCardDTO newsCardDTO2 = createNewsCardDTO(2L, CategoryType.ECONOMY.name(), LocalDateTime.now(), false);
        NewsCardDTO newsCardDTO3 = createNewsCardDTO(3L, CategoryType.ENTERTAINMENT.toString(), LocalDateTime.now(), true);

        List<NewsCardDTO> newsList = List.of(newsCardDTO1, newsCardDTO2, newsCardDTO3);
        NewsListResponse response = new NewsListResponse(newsList, NewsServiceConstant.PAGE_SIZE * 2, false);
        given(newsService.getSearchNewsCardPage(USER_ID, tags, NewsServiceConstant.PAGE_SIZE)).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/news/search")
                                .param("tags", tags.toArray(new String[0]))
                                .param("offset", String.valueOf(NewsServiceConstant.PAGE_SIZE))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.SEARCHED_NEWS_CARD_FETCH_MORE_SUCCESS))
                .andExpect(jsonPath("$.data.newsList.size()").value(3))
                .andExpect(jsonPath("$.data.offset").value(NewsServiceConstant.PAGE_SIZE * 2))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void 로그아웃_상태에서_뉴스_상세_정보_조회_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        Long newsId = 1L;
        NewsDetailDTO response = createNewsDetailDTO(newsId, false);
        given(newsService.getNewsDetail(newsId, null)).willReturn(response);

        // when & then
        mockMvc.perform(get("/news/{newsId}", newsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NEWS_DETAIL_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.news.id").value(newsId))
                .andExpect(jsonPath("$.data.news.bookmarked").value(false));                ;
    }

    @Test
    void 로그인_상태에서_뉴스_상세_정보_조회_검증() throws Exception {
        // given
        Long newsId = 1L;
        NewsDetailDTO response = createNewsDetailDTO(newsId, false);
        given(newsService.getNewsDetail(newsId, USER_ID)).willReturn(response);

        // when & then
        mockMvc.perform(get("/news/{newsId}", newsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NEWS_DETAIL_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.news.id").value(newsId))
                .andExpect(jsonPath("$.data.news.bookmarked").value(false));                ;
    }

    @Test
    void 로그아웃_상태에서_뉴스_생성_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        NewsCreateRequest request = new NewsCreateRequest(List.of("키워드1", "키워드1"));
        mockMvc.perform(
                post("/news")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인_상태에서_뉴스_생성_검증() throws Exception {
        // given
        Long newsId = 1L;
        NewsDetailDTO response = createNewsDetailDTO(newsId, true);
        given(newsService.save(eq(USER_ID), eq(false), any(NewsCreateRequest.class))).willReturn(response);

        // when & then
        NewsCreateRequest request = new NewsCreateRequest(List.of("키워드1", "키워드1"));
        mockMvc.perform(
                post("/news")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NEWS_CREATED_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.news.id").value(newsId));
    }

    @Test
    void 로그아웃_상태에서_KTB_뉴스_생성_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        TimelineCardDTO timelineCardDTO = createTimelineCardDTO();
        KtbNewsCreateRequest request = new KtbNewsCreateRequest(
                "제목",
                "미리보기 내용",
                "이미지 url",
                List.of(timelineCardDTO, timelineCardDTO, timelineCardDTO)
        );
        mockMvc.perform(
                        post("/news/ktb")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 일반_회원_KTB_뉴스_생성_불가_검증() throws Exception {
        // given
        given(newsService.saveKtbNews(eq(USER_ID), any(KtbNewsCreateRequest.class)))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, ResponseMessage.USER_NOT_CERTIFICATION));

        // when & then
        TimelineCardDTO timelineCardDTO = createTimelineCardDTO();
        KtbNewsCreateRequest request = new KtbNewsCreateRequest(
                "제목",
                "미리보기 내용",
                "이미지 url",
                List.of(timelineCardDTO, timelineCardDTO, timelineCardDTO)
        );
        mockMvc.perform(
                        post("/news/ktb")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_회원_KTB_뉴스_생성_검증() throws Exception {
        // given
        User admin = User.builder()
                .id(2L)
                .username("관리자")
                .role(Role.ADMIN)
                .build();
        UserDetailsImpl principal = new UserDetailsImpl(admin);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        Long newsId = 1L;
        NewsDetailDTO response = createNewsDetailDTO(newsId, false);
        given(newsService.saveKtbNews(eq(admin.getId()), any(KtbNewsCreateRequest.class))).willReturn(response);

        // when & then
        TimelineCardDTO timelineCardDTO = createTimelineCardDTO();
        KtbNewsCreateRequest request = new KtbNewsCreateRequest(
                "제목",
                "미리보기 내용",
                "이미지 url",
                List.of(timelineCardDTO, timelineCardDTO, timelineCardDTO)
        );
        mockMvc.perform(
                        post("/news/ktb")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NEWS_CREATED_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.news.id").value(newsId));
    }

    @Test
    void 로그아웃_상태에서_뉴스_업데이트_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        Long newsId = 1L;
        mockMvc.perform(patch("/news/{newsId}", newsId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인_상태에서_뉴스_업데이트_검증() throws Exception {
        // given
        Long newsId = 1L;
        NewsDetailDTO response = createNewsDetailDTO(newsId, true);
        given(newsService.update(eq(newsId), eq(USER_ID), eq(false))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/news/{newsId}", newsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NEWS_UPDATED_SUCCESS))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.news.id").value(newsId));
    }

    @Test
    void 관리자가_뉴스_삭제_검증() throws Exception {
        // given
        User user2 = User.builder()
                .id(USER_ID)
                .username("테스트유저")
                .role(Role.ADMIN)
                .build();

        UserDetailsImpl principal = new UserDetailsImpl(user2);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        Long newsId = 1L;

        // when & then
        mockMvc.perform(delete("/news/{newsId}", newsId))
                .andExpect(status().isNoContent());
    }

    @Test
    void 로그아웃_상태에서_뉴스_삭제_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();
        Long newsId = 1L;

        // when & then
        mockMvc.perform(delete("/news/{newsId}", newsId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseMessage.USER_NOT_CERTIFICATION));
    }

    @Test
    void 관리자가_아니면_뉴스_삭제_불가_검증() throws Exception {
        // given
        Long newsId = 1L;

        // when & then
        mockMvc.perform(delete("/news/{newsId}", newsId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(NewsResponseMessage.NEWS_DELETE_FORBIDDEN));
    }
}
