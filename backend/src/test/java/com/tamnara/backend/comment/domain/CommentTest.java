package com.tamnara.backend.comment.domain;

import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentTest {

    @Test
    void 댓글_기본값_초기화_검증() {
        // given & when
        Comment comment = new Comment();

        // then
        assertThat(comment.getId()).isNull();
        assertThat(comment.getContent()).isNull();
        assertThat(comment.getUser()).isNull();
        assertThat(comment.getNews()).isNull();
        assertThat(comment.getCreatedAt()).isNull();
    }

    @Test
    void 댓글에_뉴스와_회원과_내용_설정_검증() {
        // given
        Comment comment = new Comment();
        News news = new News();
        User user = User.builder().build();

        // when
        comment.setContent("댓글 내용");
        comment.setNews(news);
        comment.setUser(user);

        // then
        assertThat(comment.getContent()).isEqualTo("댓글 내용");
        assertThat(comment.getNews()).isEqualTo(news);
        assertThat(comment.getUser()).isEqualTo(user);
    }

    @Test
    void ID가_동일하면_동일한_댓글_검증() {
        // given
        Comment comment1 = new Comment();
        Comment comment2 = new Comment();

        // when
        comment1.setId(1L);
        comment2.setId(1L);

        // then
        assertThat(comment1).isEqualTo(comment2);
    }
}
