package com.tamnara.backend.comment.service;

import com.tamnara.backend.comment.domain.Comment;
import com.tamnara.backend.comment.dto.CommentCreateRequest;
import com.tamnara.backend.comment.dto.CommentDTO;
import com.tamnara.backend.comment.repository.CommentRepository;
import com.tamnara.backend.news.repository.NewsRepository;
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

    private final NewsRepository newsRepository;
    private final CommentRepository commentRepository;

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
                    null,
                    c.getContent(),
                    c.getCreatedAt()
            );
            commentDTOS.add(dto);
        }
        return commentDTOS;
    }

    @Override
    public Long save(Long newsId, CommentCreateRequest commentCreateRequest) {
        if (!newsRepository.existsById(newsId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 댓글의 뉴스가 존재하지 않습니다.");
        }

        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        return commentRepository.save(comment).getId();
    }

    @Override
    public Long delete(Long newsId, Long commentId) {
        if (!newsRepository.existsById(newsId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 댓글의 뉴스가 존재하지 않습니다.");
        }

        commentRepository.deleteById(commentId);
        return commentId;
    }
}
