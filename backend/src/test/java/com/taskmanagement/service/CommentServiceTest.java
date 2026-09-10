package com.taskmanagement.service;

import com.taskmanagement.dto.request.CommentRequest;
import com.taskmanagement.dto.response.CommentResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.*;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.CommentMapper;
import com.taskmanagement.repository.CommentRepository;
import com.taskmanagement.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private User regularUser;
    private Task testTask;
    private Comment testComment;
    private CommentResponse testCommentResponse;

    @BeforeEach
    void setUp() {
        regularUser = new User(3L, "User Charlie", "charlie@example.com", "pass", Role.USER);
        testTask = new Task(100L, "Task", "Desc", TaskPriority.MEDIUM, TaskStatus.TODO, null, null, null, null);
        testComment = new Comment(50L, "Great progress on this!", testTask, regularUser);
        testCommentResponse = new CommentResponse(
                50L, "Great progress on this!", 100L,
                new UserResponse(3L, "User Charlie", "charlie@example.com", Role.USER, null, null),
                null, null
        );

        CustomUserDetails userDetails = CustomUserDetails.build(regularUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should successfully add a comment to a task")
    void addComment_Success() {
        CommentRequest request = new CommentRequest("Great progress on this!");

        when(taskService.getTaskEntityById(100L)).thenReturn(testTask);
        when(userService.getUserEntityById(3L)).thenReturn(regularUser);
        when(commentMapper.toEntity(request, testTask, regularUser)).thenReturn(testComment);
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMapper.toResponse(testComment)).thenReturn(testCommentResponse);

        CommentResponse response = commentService.addComment(100L, request);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Great progress on this!");
        verify(commentRepository, times(1)).save(testComment);
    }

    @Test
    @DisplayName("Should retrieve all comments for a task")
    void getCommentsByTaskId_Success() {
        when(taskService.getTaskEntityById(100L)).thenReturn(testTask);
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(100L)).thenReturn(List.of(testComment));
        when(commentMapper.toResponse(testComment)).thenReturn(testCommentResponse);

        List<CommentResponse> comments = commentService.getCommentsByTaskId(100L);

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Should allow comment author to delete comment")
    void deleteComment_AsAuthor_Success() {
        when(commentRepository.findById(50L)).thenReturn(Optional.of(testComment));

        commentService.deleteComment(50L);

        verify(commentRepository, times(1)).delete(testComment);
    }

    @Test
    @DisplayName("Should forbid other users from deleting author's comment")
    void deleteComment_AsUnauthorizedUser_ThrowsUnauthorizedException() {
        User otherUser = new User(99L, "Other User", "other@example.com", "pass", Role.USER);
        CustomUserDetails userDetails = CustomUserDetails.build(otherUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(commentRepository.findById(50L)).thenReturn(Optional.of(testComment));

        assertThatThrownBy(() -> commentService.deleteComment(50L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You are not authorized to delete this comment");

        verify(commentRepository, never()).delete(any());
    }
}
