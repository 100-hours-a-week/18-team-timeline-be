package com.tamnara.backend.comment.service;

import com.tamnara.backend.comment.domain.Comment;
import com.tamnara.backend.comment.dto.request.CommentCreateRequest;
import com.tamnara.backend.comment.dto.CommentDTO;
import com.tamnara.backend.comment.repository.CommentRepository;
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

    @Override
    public List<CommentDTO> getComments(Long newsId, Integer page, Integer size) {
        if (!newsRepository.existsById(newsId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 댓글의 뉴스가 존재하지 않습니다.");
        }

        Page<Comment> comments = commentRepository.findAllByNewsId(newsId, PageRequest.of(page, size));

        List<CommentDTO> commentDTOS = new ArrayList<>();
        for (Comment c : comments.getContent()) {
            CommentDTO dto = new CommentDTO(
                    c.getId(),
                    c.getUser().getId(),
                    c.getContent(),
                    c.getCreatedAt()
            );
            commentDTOS.add(dto);
        }

        return commentDTOS;
    }

    @Override
    public Long save(Long userId, Long newsId, CommentCreateRequest commentCreateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 댓글의 뉴스가 존재하지 않습니다."));

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 댓글의 뉴스가 존재하지 않습니다."));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 댓글이 존재하지 않습니다."));

        if (comment.getUser().equals(user)) {
            commentRepository.delete(comment);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글을 삭제할 권한이 없습니다.");
        }
    }
}
