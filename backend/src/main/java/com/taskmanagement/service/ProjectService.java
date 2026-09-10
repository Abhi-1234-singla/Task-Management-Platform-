package com.taskmanagement.service;

import com.taskmanagement.dto.request.ProjectRequest;
import com.taskmanagement.dto.response.ProjectResponse;
import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.TaskStatus;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.ProjectNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.ProjectMapper;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository,
                          TaskRepository taskRepository,
                          UserService userService,
                          ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.projectMapper = projectMapper;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User creator = userService.getUserEntityById(currentUserId);

        Project project = projectMapper.toEntity(request, creator);
        Project savedProject = projectRepository.save(project);
        log.info("User {} created project with id {}", creator.getEmail(), savedProject.getId());

        return projectMapper.toResponse(savedProject, 0, 0);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = getProjectEntityById(id);
        return mapToProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = getProjectEntityById(id);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project {} updated successfully", id);

        return mapToProjectResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!SecurityUtils.isAdmin()) {
            throw new UnauthorizedException("Only administrators can delete projects");
        }

        Project project = getProjectEntityById(id);
        projectRepository.delete(project);
        log.info("Project {} deleted by admin {}", id, SecurityUtils.getCurrentUserEmail());
    }

    @Transactional(readOnly = true)
    public Project getProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        List<Task> tasks = taskRepository.findByProjectId(project.getId());
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();

        return projectMapper.toResponse(project, totalTasks, completedTasks);
    }
}
