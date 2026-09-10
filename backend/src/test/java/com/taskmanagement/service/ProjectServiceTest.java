package com.taskmanagement.service;

import com.taskmanagement.dto.request.ProjectRequest;
import com.taskmanagement.dto.response.ProjectResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.ProjectStatus;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.ProjectNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.ProjectMapper;
import com.taskmanagement.repository.ProjectRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private User managerUser;
    private Project testProject;
    private ProjectResponse testProjectResponse;

    @BeforeEach
    void setUp() {
        managerUser = new User(2L, "Manager Bob", "bob@example.com", "pass", Role.MANAGER);
        testProject = new Project(10L, "Alpha Project", "Description for Alpha", ProjectStatus.ACTIVE, managerUser);
        testProjectResponse = new ProjectResponse(
                10L, "Alpha Project", "Description for Alpha", ProjectStatus.ACTIVE,
                new UserResponse(2L, "Manager Bob", "bob@example.com", Role.MANAGER, null, null),
                null, null, 0, 0
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
    @DisplayName("Should successfully create project by manager")
    void createProject_Success() {
        ProjectRequest request = new ProjectRequest("Alpha Project", "Description for Alpha", ProjectStatus.ACTIVE);

        when(userService.getUserEntityById(2L)).thenReturn(managerUser);
        when(projectMapper.toEntity(request, managerUser)).thenReturn(testProject);
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toResponse(testProject, 0, 0)).thenReturn(testProjectResponse);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Alpha Project");
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    @DisplayName("Should retrieve all projects with task aggregation metrics")
    void getAllProjects_Success() {
        when(projectRepository.findAll()).thenReturn(List.of(testProject));
        when(taskRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(projectMapper.toResponse(eq(testProject), eq(0L), eq(0L))).thenReturn(testProjectResponse);

        List<ProjectResponse> responses = projectService.getAllProjects();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should retrieve project by ID")
    void getProjectById_Success() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(projectMapper.toResponse(eq(testProject), eq(0L), eq(0L))).thenReturn(testProjectResponse);

        ProjectResponse response = projectService.getProjectById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should throw ProjectNotFoundException when project ID does not exist")
    void getProjectById_NotFound_ThrowsException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(99L))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("Project not found with id: 99");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when non-admin attempts to delete project")
    void deleteProject_AsManager_ThrowsUnauthorizedException() {
        assertThatThrownBy(() -> projectService.deleteProject(10L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only administrators can delete projects");

        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should allow admin to delete project")
    void deleteProject_AsAdmin_Success() {
        User adminUser = new User(1L, "Admin Alice", "alice@example.com", "pass", Role.ADMIN);
        CustomUserDetails adminDetails = CustomUserDetails.build(adminUser);
        UsernamePasswordAuthenticationToken adminAuth =
                new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(testProject));

        projectService.deleteProject(10L);

        verify(projectRepository, times(1)).delete(testProject);
    }
}
