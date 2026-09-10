package com.taskmanagement.service;

import com.taskmanagement.dto.request.TaskRequest;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.*;
import com.taskmanagement.exception.TaskNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.TaskMapper;
import com.taskmanagement.repository.CommentRepository;
import com.taskmanagement.repository.TaskRepository;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private User managerUser;
    private User regularUser;
    private Project testProject;
    private Task testTask;
    private TaskResponse testTaskResponse;

    @BeforeEach
    void setUp() {
        managerUser = new User(2L, "Manager Bob", "bob@example.com", "pass", Role.MANAGER);
        regularUser = new User(3L, "User Charlie", "charlie@example.com", "pass", Role.USER);
        testProject = new Project(10L, "Alpha Project", "Desc", ProjectStatus.ACTIVE, managerUser);

        testTask = new Task(
                100L, "Build REST API", "Implement CRUD", TaskPriority.HIGH, TaskStatus.TODO,
                LocalDate.now().plusDays(5), testProject, regularUser, managerUser
        );

        testTaskResponse = new TaskResponse(
                100L, "Build REST API", "Implement CRUD", TaskPriority.HIGH, TaskStatus.TODO,
                LocalDate.now().plusDays(5), 10L, "Alpha Project",
                new UserResponse(3L, "User Charlie", "charlie@example.com", Role.USER, null, null),
                new UserResponse(2L, "Manager Bob", "bob@example.com", Role.MANAGER, null, null),
                null, null, 0
        );

        CustomUserDetails userDetails = CustomUserDetails.build(managerUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should successfully create task with project and assignee assignment")
    void createTask_Success() {
        TaskRequest request = new TaskRequest(
                "Build REST API", "Implement CRUD", TaskPriority.HIGH, TaskStatus.TODO,
                LocalDate.now().plusDays(5), 10L, 3L
        );

        when(userService.getUserEntityById(2L)).thenReturn(managerUser);
        when(projectService.getProjectEntityById(10L)).thenReturn(testProject);
        when(userService.getUserEntityById(3L)).thenReturn(regularUser);
        when(taskMapper.toEntity(request, testProject, regularUser, managerUser)).thenReturn(testTask);
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toResponse(testTask, 0)).thenReturn(testTaskResponse);

        TaskResponse response = taskService.createTask(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Build REST API");
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    @DisplayName("Should retrieve task by ID with comment count")
    void getTaskById_Success() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));
        when(commentRepository.countByTaskId(100L)).thenReturn(3L);
        when(taskMapper.toResponse(testTask, 3L)).thenReturn(testTaskResponse);

        TaskResponse response = taskService.getTaskById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when task does not exist")
    void getTaskById_NotFound_ThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
    }

    @Test
    @DisplayName("Should allow assigned user to update their task status")
    void updateTaskStatus_ByAssignedUser_Success() {
        CustomUserDetails userDetails = CustomUserDetails.build(regularUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(commentRepository.countByTaskId(100L)).thenReturn(0L);
        when(taskMapper.toResponse(testTask, 0L)).thenReturn(testTaskResponse);

        TaskResponse response = taskService.updateTaskStatus(100L, TaskStatus.IN_PROGRESS);

        assertThat(response).isNotNull();
        assertThat(testTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    @DisplayName("Should reject status update by unauthorized user not assigned to the task")
    void updateTaskStatus_ByUnauthorizedUser_ThrowsUnauthorizedException() {
        User otherUser = new User(99L, "Other User", "other@example.com", "pass", Role.USER);
        CustomUserDetails userDetails = CustomUserDetails.build(otherUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.updateTaskStatus(100L, TaskStatus.COMPLETED))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You are not authorized to update the status of this task");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete task successfully")
    void deleteTask_Success() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));

        taskService.deleteTask(100L);

        verify(taskRepository, times(1)).delete(testTask);
    }
}
