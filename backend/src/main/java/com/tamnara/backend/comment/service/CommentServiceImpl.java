package com.tamnara.backend.comment.service;

import com.tamnara.backend.comment.constant.CommentResponseMessage;
import com.tamnara.backend.comment.constant.CommentServiceConstant;
import com.tamnara.backend.comment.domain.Comment;
import com.tamnara.backend.comment.dto.CommentDTO;
import com.tamnara.backend.comment.dto.request.CommentCreateRequest;
import com.tamnara.backend.comment.dto.response.CommentListResponse;
import com.tamnara.backend.comment.repository.CommentRepository;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;

    @Override
    public CommentListResponse getComments(Long newsId, Integer offset) {
        log.info("[COMMENT] getComments 시작 - newsId:{}", newsId);

        if (!newsRepository.existsById(newsId)) {
            log.error("[COMMENT] getComments 실패 - 뉴스가 존재하지 않음, newsId:{}", newsId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND);
        }
        log.info("[COMMENT] getComments 처리 중 - 뉴스 조회 성공, newsId:{}", newsId);

        int page = offset / CommentServiceConstant.PAGE_SIZE;
        int nextOffset = (page + 1) * CommentServiceConstant.PAGE_SIZE;

        Page<Comment> comments = commentRepository.findAllByNewsIdOrderByIdDesc(newsId, PageRequest.of(page, CommentServiceConstant.PAGE_SIZE));
        boolean hasNext = !commentRepository.findAllByNewsIdOrderByIdDesc(newsId, PageRequest.of(page + 1, CommentServiceConstant.PAGE_SIZE)).isEmpty();

        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (Comment c : comments.getContent()) {
            CommentDTO dto = new CommentDTO(
                    c.getId(),
                    c.getUser() != null ? c.getUser().getId() : null,
                    c.getUser() != null ? c.getUser().getUsername() : null,
                    c.getContent(),
                    c.getCreatedAt()
            );
            commentDTOList.add(dto);
        }

        log.info("[COMMENT] getComments 완료 - newsId:{}", newsId);
        return new CommentListResponse(
                commentDTOList,
                nextOffset,
                hasNext
        );
    }

    @Override
    public Long save(Long userId, Long newsId, CommentCreateRequest commentCreateRequest) {
        log.info("[COMMENT] save 시작 - userId:{} newsId:{}", userId, newsId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));
        log.info("[COMMENT] save 처리 중 - 회원 조회 성공, userId:{} newsId:{}", userId, newsId);

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));
        log.info("[COMMENT] save 처리 중 - 뉴스 조회 성공, userId:{} newsId:{}", userId, newsId);

        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        comment.setUser(user);
        comment.setNews(news);
        commentRepository.save(comment);

        log.info("[COMMENT] save 완료 - userId:{} newsId:{}", userId, newsId);
        return comment.getId();
    }

    @Override
    public void delete(Long userId, Long newsId, Long commentId) {
        log.info("[COMMENT] delete 시작 - userId:{} newsId:{} commentId:{}", userId, newsId, commentId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        log.info("[COMMENT] delete 처리 중 - 회원 조회 성공, userId:{} newsId:{} commentId:{}", userId, newsId, commentId);

        newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));
        log.info("[COMMENT] delete 처리 중 - 뉴스 조회 성공, userId:{} newsId:{} commentId:{}", userId, newsId, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CommentResponseMessage.COMMENT_NOT_FOUND));
        log.info("[COMMENT] delete 처리 중 - 댓글 조회 성공, userId:{} newsId:{} commentId:{}", userId, newsId, commentId);

        if (comment.getUser().equals(user)) {
            commentRepository.delete(comment);
            log.info("[COMMENT] delete 완료 - userId:{} newsId:{} commentId:{}", userId, newsId, commentId);
        } else {
            log.error("[COMMENT] delete 실패 - 댓글 작성자와 삭제 요청자가 일치하지 않음, userId:{} newsId:{} commentId:{}", userId, newsId, commentId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, CommentResponseMessage.COMMENT_DELETE_FORBIDDEN);
        }
    }
}
