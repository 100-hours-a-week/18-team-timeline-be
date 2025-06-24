package com.tamnara.backend.comment.service;

import com.tamnara.backend.comment.constant.CommentResponseMessage;
import com.tamnara.backend.comment.constant.CommentServiceConstant;
import com.tamnara.backend.comment.domain.Comment;
import com.tamnara.backend.comment.dto.request.CommentCreateRequest;
import com.tamnara.backend.comment.dto.response.CommentListResponse;
import com.tamnara.backend.comment.repository.CommentRepository;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock private CommentRepository commentRepository;
    @Mock private UserRepository userRepository;
    @Mock private NewsRepository newsRepository;

    @InjectMocks private CommentServiceImpl commentServiceImpl;

    private User user;
    private News news;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(1L);

        news = mock(News.class);
        lenient().when(news.getId()).thenReturn(1L);
    }

    private Comment createComment(User user, News news) {
        Comment comment = new Comment();
        comment.setContent("댓글 내용");
        comment.setUser(user);
        comment.setNews(news);
        return comment;
    }

    @Test
    void 댓글_목록_조회_검증() {
        // given
        Comment comment1 = createComment(user, news);
        Comment comment2 = createComment(user, news);
        Comment comment3 = createComment(user, news);
        Page<Comment> commentPage = new PageImpl<>(Arrays.asList(comment1, comment2, comment3));

        when(newsRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findAllByNewsIdOrderByIdDesc(news.getId(), PageRequest.of(0, CommentServiceConstant.PAGE_SIZE))).thenReturn(commentPage);
        when(commentRepository.findAllByNewsIdOrderByIdDesc(news.getId(), PageRequest.of(1, CommentServiceConstant.PAGE_SIZE))).thenReturn(Page.empty());

        // when
        CommentListResponse response = commentServiceImpl.getComments(news.getId(), 0);

        // then
        assertEquals(3, response.getComments().size());
        assertEquals(CommentServiceConstant.PAGE_SIZE, response.getOffset());
        assertFalse(response.isHasNext());
    }

    @Test
    void 댓글_저장_검증() {
        // given
        Long commentId = 1L;
        Comment comment = createComment(user, news);
        CommentCreateRequest request = new CommentCreateRequest(comment.getContent());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            savedComment.setId(commentId);
            return savedComment;
        });

        // when
        Long returnedCommentId = commentServiceImpl.save(user.getId(), news.getId(), request);

        // then
        assertEquals(commentId, returnedCommentId);
    }

    @Test
    void 댓글_삭제_검증() {
        // given
        Comment comment = createComment(user, news);
        comment.setId(1L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        // when
        commentServiceImpl.delete(user.getId(), news.getId(), comment.getId());

        // then
        assertFalse(commentRepository.existsById(comment.getId()));
    }

    @Test
    void 내_댓글_아닌_댓글_삭제_불가_검증() {
        // given
        User writer = User.builder()
                .id(user.getId())
                .username("작성자")
                .build();

        User anotherUser = User.builder()
                .id(user.getId() + 1)
                .username("다른 사용자")
                .build();

        Comment comment = createComment(writer, news);
        comment.setId(1L);

        when(userRepository.findById(anotherUser.getId())).thenReturn(Optional.of(anotherUser));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            commentServiceImpl.delete(anotherUser.getId(), news.getId(), comment.getId());
        });

        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(CommentResponseMessage.COMMENT_DELETE_FORBIDDEN, exception.getReason());
    }
}
