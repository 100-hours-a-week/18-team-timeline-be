package com.tamnara.backend.news.controller;

import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.dto.NewsDetailDTO;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.HotissueNewsListResponse;
import com.tamnara.backend.news.dto.response.NewsDetailResponse;
import com.tamnara.backend.news.dto.response.NewsListResponse;
import com.tamnara.backend.news.dto.response.category.AllResponse;
import com.tamnara.backend.news.dto.response.category.EconomyResponse;
import com.tamnara.backend.news.dto.response.category.EntertainmentResponse;
import com.tamnara.backend.news.dto.response.category.KtbResponse;
import com.tamnara.backend.news.dto.response.category.MultiCategoryResponse;
import com.tamnara.backend.news.dto.response.category.SportsResponse;
import com.tamnara.backend.news.service.NewsService;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    private final Integer PAGE_SIZE = 20;

    @GetMapping("/hotissue")
    public ResponseEntity<WrappedDTO<HotissueNewsListResponse>> findHotissueNews() {
        try {
            List<NewsCardDTO> newsCards = newsService.getHotissueNewsCardPage();

            HotissueNewsListResponse newsList = new HotissueNewsListResponse(newsCards);

            return ResponseEntity.status(HttpStatus.OK).body(
                    new WrappedDTO<>(
                            true,
                            "요청하신 핫이슈 뉴스 카드 목록을 성공적으로 불러왔습니다.",
                            newsList
                    ));
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<WrappedDTO<?>> findNormalNews(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") Integer offset
    ) {
        try {
            if (offset == 0) {
                Long userId = (userDetails != null && userDetails.getUser() != null)
                        ? userDetails.getUser().getId()
                        : null;

                MultiCategoryResponse data = new MultiCategoryResponse();

                data.setAll(
                        new NewsListResponse(
                                newsService.getNormalNewsCardPage(userId, 0, PAGE_SIZE),
                                PAGE_SIZE,
                                !newsService.getNormalNewsCardPage(userId, 1, PAGE_SIZE).isEmpty()
                        )
                );

                data.setEconomy(
                        new NewsListResponse(
                                newsService.getNormalNewsCardPageByCategory(userId, CategoryType.ECONOMY.name(), 0, PAGE_SIZE),
                                PAGE_SIZE,
                                !newsService.getNormalNewsCardPageByCategory(userId, CategoryType.ECONOMY.name(), 1, PAGE_SIZE).isEmpty()
                        )
                );

                data.setEntertainment(
                        new NewsListResponse(
                                newsService.getNormalNewsCardPageByCategory(userId, CategoryType.ENTERTAINMENT.name(), 0, PAGE_SIZE),
                                PAGE_SIZE,
                                !newsService.getNormalNewsCardPageByCategory(userId, CategoryType.ENTERTAINMENT.name(), 1, PAGE_SIZE).isEmpty()
                        )
                );

                data.setSports(
                        new NewsListResponse(
                                newsService.getNormalNewsCardPageByCategory(userId, CategoryType.SPORTS.name(), 0, PAGE_SIZE),
                                PAGE_SIZE,
                                !newsService.getNormalNewsCardPageByCategory(userId, CategoryType.SPORTS.name(), 1, PAGE_SIZE).isEmpty()
                        )
                );

                data.setKtb(
                        new NewsListResponse(
                                newsService.getNormalNewsCardPageByCategory(userId, CategoryType.KTB.name(), 0, PAGE_SIZE),
                                PAGE_SIZE,
                                !newsService.getNormalNewsCardPageByCategory(userId, CategoryType.KTB.name(), 1, PAGE_SIZE).isEmpty()
                        )
                );

                return ResponseEntity.ok(new WrappedDTO<>(
                        true,
                        "요청하신 일반 뉴스 카드 목록을 성공적으로 불러왔습니다.",
                        data
                ));
            } else {
                Long userId = (userDetails != null && userDetails.getUser() != null)
                        ? userDetails.getUser().getId()
                        : null;

                int pageNum = offset / PAGE_SIZE;
                int nextOffset = (pageNum + 1) * PAGE_SIZE;

                if (Objects.equals(category, "ALL")) {
                    List<NewsCardDTO> newsCards = newsService.getNormalNewsCardPage(userId, pageNum, PAGE_SIZE);
                    boolean hasNext = !newsService.getNormalNewsCardPage(userId, pageNum + 1, PAGE_SIZE).isEmpty();

                    return ResponseEntity.status(HttpStatus.OK).body(
                            new WrappedDTO<>(
                                    true,
                                    "요청하신 전체 카테고리의 일반 뉴스 카드 목록을 성공적으로 불러왔습니다.",
                                    new AllResponse(
                                            new NewsListResponse(
                                                    newsCards,
                                                    nextOffset,
                                                    hasNext
                                            )
                                    )));
                } else {
                    List<NewsCardDTO> newsCards = newsService.getNormalNewsCardPageByCategory(userId, category, pageNum, PAGE_SIZE);
                    boolean hasNext = !newsService.getNormalNewsCardPageByCategory(userId, category, pageNum + 1, PAGE_SIZE).isEmpty();

                    if (Objects.equals(category, CategoryType.ECONOMY.name())) {
                        return ResponseEntity.status(HttpStatus.OK).body(
                                new WrappedDTO<>(
                                        true,
                                        "요청하신 경제 카테고리의 일반 뉴스 카드 목록을 성공적으로 불러왔습니다.",
                                        new EconomyResponse(
                                                new NewsListResponse(
                                                        newsCards,
                                                        nextOffset,
                                                        hasNext
                                                )
                                        )));
                    } else if (Objects.equals(category, CategoryType.ENTERTAINMENT.name())) {
                        return ResponseEntity.status(HttpStatus.OK).body(
                                new WrappedDTO<>(
                                        true,
                                        "요청하신 연예 카테고리의 일반 뉴스 카드 목록을 성공적으로 불러왔습니다.",
                                        new EntertainmentResponse(
                                                new NewsListResponse(
                                                        newsCards,
                                                        nextOffset,
                                                        hasNext
                                                )
                                        )));
                    } else if (Objects.equals(category, CategoryType.SPORTS.name())) {
                        return ResponseEntity.status(HttpStatus.OK).body(
                                new WrappedDTO<>(
                                        true,
                                        "요청하신 스포츠 카테고리의 일반 뉴스 카드 목록을 성공적으로 불러왔습니다.",
                                        new SportsResponse(
                                                new NewsListResponse(
                                                        newsCards,
                                                        nextOffset,
                                                        hasNext
                                                )
                                        )));
                    } else if (Objects.equals(category, CategoryType.KTB.name())) {
                        return ResponseEntity.status(HttpStatus.OK).body(
                                new WrappedDTO<>(
                                        true,
                                        "요청하신 KTB 카테고리의 일반 뉴스 카드 목록을 성공적으로 불러왔습니다.",
                                        new KtbResponse(
                                                new NewsListResponse(
                                                        newsCards,
                                                        nextOffset,
                                                        hasNext
                                                )
                                        )));
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            }
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<WrappedDTO<NewsDetailResponse>> findNewsDetail(@PathVariable("newsId") Long newsId,
                                                                         @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Long userId = null;
            if (!(userDetails == null || userDetails.getUser() == null)) {
                userId = userDetails.getUser().getId();
            }

            NewsDetailDTO res = newsService.getNewsDetail(newsId, userId);
            NewsDetailResponse data = new NewsDetailResponse(res);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            new WrappedDTO<>(
                                    true,
                                    "요청하신 뉴스의 상세 정보를 성공적으로 불러왔습니다.",
                                    data
                            )
                    );
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<WrappedDTO<NewsDetailResponse>> createNews(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                             @RequestBody NewsCreateRequest req
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 회원입니다.");
            }

            Long userId = userDetails.getUser().getId();

            NewsDetailDTO res = newsService.save(userId, false, req);
            if (res == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            NewsDetailResponse data = new NewsDetailResponse(res);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new WrappedDTO<>(
                            true,
                            "뉴스가 성공적으로 생성되었습니다.",
                            data
                    ));
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PatchMapping("/{newsId}")
    public ResponseEntity<WrappedDTO<NewsDetailResponse>> updateNews(@PathVariable Long newsId,
                                                                             @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 회원입니다.");
            }

            Long userId = userDetails.getUser().getId();

            NewsDetailDTO res = newsService.update(newsId, userId);
            if (res == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            NewsDetailResponse data = new NewsDetailResponse(res);

            return ResponseEntity.status(HttpStatus.OK).body(
                    new WrappedDTO<>(
                            true,
                            "데이터가 성공적으로 업데이트되었습니다.",
                            data
                    ));
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping("/{newsId}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long newsId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 회원입니다.");
            }

            if (userDetails.getUser().getRole() != Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "뉴스를 삭제할 권한이 없습니다.");
            }

            newsService.delete(newsId, userDetails.getUser().getId());

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
