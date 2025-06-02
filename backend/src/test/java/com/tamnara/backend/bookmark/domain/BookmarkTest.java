package com.tamnara.backend.bookmark.domain;

import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BookmarkTest {

    @Test
    void 댓글_기본값_초기화_검증() {
        // given & when
        Bookmark comment = new Bookmark();

        // then
        assertThat(comment.getId()).isNull();
        assertThat(comment.getUser()).isNull();
        assertThat(comment.getNews()).isNull();
        assertThat(comment.getCreatedAt()).isNull();
    }

    @Test
    void 댓글에_뉴스와_회원_설정_검증() {
        // given
        Bookmark comment = new Bookmark();
        News news = new News();
        User user = User.builder().build();

        // when
        comment.setNews(news);
        comment.setUser(user);

        // then
        assertThat(comment.getNews()).isEqualTo(news);
        assertThat(comment.getUser()).isEqualTo(user);
    }

    @Test
    void ID가_동일하면_동일한_댓글_검증() {
        // given
        Bookmark comment1 = new Bookmark();
        Bookmark comment2 = new Bookmark();

        // when
        comment1.setId(1L);
        comment2.setId(1L);

        // then
        assertThat(comment1).isEqualTo(comment2);
    }
}
