package com.taskmanagement.service;

import com.taskmanagement.dto.request.CommentRequest;
import com.taskmanagement.dto.response.CommentResponse;
import com.taskmanagement.entity.Comment;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.CommentNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.CommentMapper;
import com.taskmanagement.repository.CommentRepository;
import com.taskmanagement.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository,
                          TaskService taskService,
                          UserService userService,
                          CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.taskService = taskService;
        this.userService = userService;
        this.commentMapper = commentMapper;
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request) {
        Task task = taskService.getTaskEntityById(taskId);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userService.getUserEntityById(currentUserId);

        Comment comment = commentMapper.toEntity(request, task, user);
        Comment savedComment = commentRepository.save(comment);
        log.info("User {} commented on task {}", user.getEmail(), taskId);

        return commentMapper.toResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTaskId(Long taskId) {
        // Ensure task exists
        taskService.getTaskEntityById(taskId);
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAuthor = Objects.equals(comment.getUser().getId(), currentUserId);
        boolean isAdmin = SecurityUtils.isAdmin();

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
        log.info("Comment {} deleted by user id {}", commentId, currentUserId);
    }
}
