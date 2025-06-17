package com.tamnara.backend.comment.service;

import com.tamnara.backend.comment.constant.CommentResponseMessage;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;

    private final Integer PAGE_SIZE = 20;

    @Override
    public CommentListResponse getComments(Long newsId, Integer offset) {
        if (!newsRepository.existsById(newsId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND);
        }

        int page = offset / PAGE_SIZE;
        int nextOffset = (page + 1) * PAGE_SIZE;

        Page<Comment> comments = commentRepository.findAllByNewsIdOrderByIdAsc(newsId, PageRequest.of(page, PAGE_SIZE));
        boolean hasNext = !commentRepository.findAllByNewsIdOrderByIdAsc(newsId, PageRequest.of(page + 1, PAGE_SIZE)).isEmpty();

        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (Comment c : comments.getContent()) {
            CommentDTO dto = new CommentDTO(
                    c.getId(),
                    c.getUser().getId(),
                    c.getContent(),
                    c.getCreatedAt()
            );
            commentDTOList.add(dto);
        }

        return new CommentListResponse(
                commentDTOList,
                nextOffset,
                hasNext
        );
    }

    @Override
    public Long save(Long userId, Long newsId, CommentCreateRequest commentCreateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        comment.setUser(user);
        comment.setNews(news);
        commentRepository.save(comment);

        return comment.getId();
    }

    @Override
    public void delete(Long userId, Long newsId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CommentResponseMessage.COMMENT_NOT_FOUND));

        if (comment.getUser().equals(user)) {
            commentRepository.delete(comment);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, CommentResponseMessage.COMMENT_DELETE_FORBIDDEN);
        }
    }
}
