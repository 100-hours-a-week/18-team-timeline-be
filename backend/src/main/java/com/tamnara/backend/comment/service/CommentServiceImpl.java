package com.tamnara.backend.comment.service;

import com.tamnara.backend.comment.domain.Comment;
import com.tamnara.backend.comment.dto.CommentCreateRequest;
import com.tamnara.backend.comment.dto.CommentDTO;
import com.tamnara.backend.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public List<CommentDTO> getComments(Long newsId, Integer page, Integer size) {
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
        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        return commentRepository.save(comment).getId();
    }

    @Override
    public Long delete(Long commentId) {
        commentRepository.deleteById(commentId);
        return commentId;
    }
}
