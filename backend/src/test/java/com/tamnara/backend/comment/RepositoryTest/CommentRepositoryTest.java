package com.tamnara.backend.comment.RepositoryTest;

import com.tamnara.backend.comment.domain.Comment;
import com.tamnara.backend.comment.repository.CommentRepository;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.repository.NewsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CommentRepositoryTest {

    @PersistenceContext private EntityManager em;

    @Autowired private CommentRepository commentRepository;
    @Autowired private NewsRepository newsRepository;

    News news;
    Comment comment1;
    Comment comment2;
    Comment comment3;

    @BeforeEach
    void setUp() {
        news = new News();
        news.setTitle("24자의 제목");
        news.setSummary("36자의 미리보기 내용");
        news.setIsHotissue(true);
        newsRepository.save(news);

        comment1 = new Comment();
        comment1.setContent("150자의 뉴스에 대한 댓글 내용");
        comment1.setNews(news);

        comment2 = new Comment();
        comment2.setContent("150자의 뉴스에 대한 댓글 내용");
        comment2.setNews(news);

        comment3 = new Comment();
        comment3.setContent("150자의 뉴스에 대한 댓글 내용");
        comment3.setNews(news);
    }

    @Test
    public void 단일_댓글_생성_테스트() {
        // given
        commentRepository.save(comment1);

        // when
        Optional<Comment> findComment = commentRepository.findById(comment1.getId());

        // then
        assertEquals(comment1.getId(), findComment.get().getId());
    }

    @Test
    public void 단일_댓글_삭제_테스트() {
        // given
        commentRepository.save(comment1);

        // when
        Optional<Comment> findComment = commentRepository.findById(comment1.getId());
        commentRepository.delete(findComment.get());

        // then
        assertFalse(newsRepository.findById(comment1.getId()).isPresent());
    }

    @Test
    public void 뉴스_연관_댓글들_정렬_페이징_조회_테스트() {
        // given
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        // when
        Pageable pageable = PageRequest.of(0, 2);
        Page<Comment> commentPage = commentRepository.findAllByNewsId(news.getId(), pageable);

        // then
        assertEquals(2, commentPage.getContent().size());
        assertTrue(commentPage.stream().allMatch(c -> c.getNews().getId().equals(news.getId())));

        Comment first = commentPage.getContent().get(0);
        Comment second = commentPage.getContent().get(1);
        assertTrue(first.getId() > second.getId());
    }

    @Test
    public void 뉴스_삭제시_연관_댓글들_지동_삭제_테스트() {
        // given
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        em.flush();
        em.clear();

        // when
        newsRepository.delete(news);

        em.flush();
        em.clear();

        // then
        assertFalse(newsRepository.findById(comment1.getId()).isPresent());
        assertFalse(newsRepository.findById(comment2.getId()).isPresent());
        assertFalse(newsRepository.findById(comment3.getId()).isPresent());
    }
}
