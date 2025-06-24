package com.tamnara.backend.comment.repository;

import com.tamnara.backend.comment.constant.CommentServiceConstant;
import com.tamnara.backend.comment.domain.Comment;
import com.tamnara.backend.config.TestConfig;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CommentRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private CommentRepository commentRepository;
    @Autowired private NewsRepository newsRepository;
    @Autowired private UserRepository userRepository;

    News news;
    User user;

    @BeforeEach
    void setUp() {
        newsRepository.deleteAll();
        userRepository.deleteAll();

        em.flush();
        em.clear();

        news = new News();
        news.setTitle("제목");
        news.setSummary("미리보기 내용");
        news.setIsHotissue(true);
        newsRepository.saveAndFlush(news);

        this.user = User.builder()
                .email("이메일")
                .password("비밀번호")
                .username("이름")
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
        userRepository.saveAndFlush(this.user);
    }

    private Comment createComment(String content, News news, User user) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setNews(news);
        comment.setUser(user);
        return comment;
    }

    @Test
    void 댓글_생성_성공_검증() {
        // given
        Comment comment = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment);

        // when
        Comment findedComment = commentRepository.findById(comment.getId()).get();

        // then
        assertEquals(findedComment.getId(), comment.getId());
    }

    @Test
    void 댓글_뉴스_필드_null_불가_검증() {
        // given
        Comment comment = createComment("댓글 내용", null, user);

        // when & then
        assertNull(comment.getNews());
        assertThrows(DataIntegrityViolationException.class, () -> {
            commentRepository.saveAndFlush(comment);
        });
    }

    @Test
    void 댓글_회원_필드_null_허용_검증() {
        // given
        Comment comment = createComment("댓글 내용", news, null);
        commentRepository.saveAndFlush(comment);

        // when
        Comment findedComment = commentRepository.findById(comment.getId()).get();

        // then
        assertEquals(findedComment.getId(), comment.getId());
        assertNull(findedComment.getUser());
    }

    @Test
    void 댓글_내용_필드_null_불가_검증() {
        // given
        Comment comment = createComment(null, news, user);

        // when & then
        assertNull(comment.getContent());
        assertThrows(DataIntegrityViolationException.class, () -> {
            commentRepository.saveAndFlush(comment);
        });
    }

    @Test
    void 댓글_내용_필드_길이_제약_검증() {
        // given
        String shortContent = "가".repeat(150);
        String longContent = "가".repeat(151);

        // when
        Comment comment1 = createComment(shortContent, news, user);
        Comment comment2 = createComment(longContent, news, user);

        // then
        commentRepository.saveAndFlush(comment1);
        assertEquals(shortContent, comment1.getContent());
        assertThrows(DataIntegrityViolationException.class, () -> {
            commentRepository.saveAndFlush(comment2);
        });
    }

    @Test
    void 댓글_수정_시_내용만_수정_가능_검증() {
        // given
        Comment comment = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment);

        // when
        News news2 = new News();
        news2.setTitle("제목");
        news2.setSummary("미리보기 내용");
        news2.setIsHotissue(true);
        newsRepository.saveAndFlush(news2);

        User user2;
        user2 = User.builder()
                .email("이메일2")
                .password("비밀번호2")
                .username("이름2")
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
        userRepository.saveAndFlush(user2);

        Comment findedComment = commentRepository.findById(comment.getId()).get();
        findedComment.setContent("새로운 댓글 내용");
        findedComment.setNews(news2);
        findedComment.setUser(user2);
        commentRepository.saveAndFlush(findedComment);

        // then
        assertEquals("새로운 댓글 내용", findedComment.getContent());
        assertEquals(comment.getNews(), findedComment.getNews());
        assertEquals(comment.getUser(), findedComment.getUser());
    }

    @Test
    void 뉴스_ID로_연관된_댓글_전체_조회_시_ID_내림차순_정렬_검증() {
        // given
        Comment comment1 = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment1);
        Comment comment2 = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment2);
        Comment comment3 = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment3);

        // when
        Pageable pageable = PageRequest.of(0, CommentServiceConstant.PAGE_SIZE);
        Page<Comment> commentPage = commentRepository.findAllByNewsIdOrderByIdDesc(news.getId(), pageable);
        List<Comment> commentList = commentPage.getContent();

        // then
        assertEquals(3, commentList.size());
        assertEquals(comment3.getId(), commentList.get(0).getId());
        assertEquals(comment2.getId(), commentList.get(1).getId());
        assertEquals(comment1.getId(), commentList.get(2).getId());
    }

    @Test
    void 댓글_삭제_성공_검증() {
        // given
        Comment comment = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment);

        // when
        Comment findedComment = commentRepository.findById(comment.getId()).get();
        commentRepository.delete(findedComment);

        // then
        assertFalse(commentRepository.existsById(comment.getId()));
    }

    @Test
    void 뉴스_삭제_시_연관된_댓글들_CASCADE_검증() {
        // given
        Comment comment1 = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment1);
        Comment comment2 = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment2);
        Comment comment3 = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment3);
        em.clear();

        // when
        newsRepository.delete(news);
        em.flush();
        em.clear();

        // then
        assertFalse(commentRepository.existsById(comment1.getId()));
        assertFalse(commentRepository.existsById(comment2.getId()));
        assertFalse(commentRepository.existsById(comment3.getId()));
    }

    @Test
    void 회원_삭제_시_연관된_댓글들의_회원_필드_SET_NULL_검증() {
        // given
        Comment comment1 = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment1);
        Comment comment2 = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment2);
        Comment comment3 = createComment("댓글 내용", news, user);
        commentRepository.saveAndFlush(comment3);
        em.clear();

        // when
        userRepository.delete(user);
        em.flush();
        em.clear();

        // then
        assertNull(commentRepository.findById(comment1.getId()).get().getUser());
        assertNull(commentRepository.findById(comment2.getId()).get().getUser());
        assertNull(commentRepository.findById(comment3.getId()).get().getUser());
    }
}
