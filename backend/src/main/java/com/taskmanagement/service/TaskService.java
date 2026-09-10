package com.taskmanagement.service;

import com.taskmanagement.dto.request.TaskRequest;
import com.taskmanagement.dto.response.PagedResponse;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.*;
import com.taskmanagement.exception.TaskNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.TaskMapper;
import com.taskmanagement.repository.CommentRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.util.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository,
                       CommentRepository commentRepository,
                       ProjectService projectService,
                       UserService userService,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User creator = userService.getUserEntityById(currentUserId);
        Project project = projectService.getProjectEntityById(request.getProjectId());

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userService.getUserEntityById(request.getAssignedToId());
        }

        Task task = taskMapper.toEntity(request, project, assignedTo, creator);
        Task savedTask = taskRepository.save(task);
        log.info("Task '{}' created in project {} by user {}", savedTask.getTitle(), project.getId(), creator.getEmail());

        return taskMapper.toResponse(savedTask, 0);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasks(TaskStatus status,
                                               TaskPriority priority,
                                               Long assigneeId,
                                               Long projectId,
                                               int page,
                                               int size,
                                               String sortBy,
                                               String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        Specification<Task> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedTo").get("id"), assigneeId));
            }
            if (projectId != null) {
                predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        List<TaskResponse> responses = taskPage.getContent().stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                responses,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = getTaskEntityById(id);
        return mapToTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = getTaskEntityById(id);
        Project project = projectService.getProjectEntityById(request.getProjectId());

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userService.getUserEntityById(request.getAssignedToId());
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setAssignedTo(assignedTo);

        Task updatedTask = taskRepository.save(task);
        log.info("Task {} updated successfully", id);

        return mapToTaskResponse(updatedTask);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        Task task = getTaskEntityById(id);
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // Check if user is admin, manager, or the assigned user
        boolean isAuthorized = SecurityUtils.isAdmin() || SecurityUtils.isManager() ||
                (task.getAssignedTo() != null && Objects.equals(task.getAssignedTo().getId(), currentUserId));

        if (!isAuthorized) {
            throw new UnauthorizedException("You are not authorized to update the status of this task");
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        log.info("Task {} status updated to {} by user id {}", id, status, currentUserId);

        return mapToTaskResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = getTaskEntityById(id);
        taskRepository.delete(task);
        log.info("Task {} deleted successfully", id);
    }

    @Transactional(readOnly = true)
    public Task getTaskEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    private TaskResponse mapToTaskResponse(Task task) {
        long commentCount = commentRepository.countByTaskId(task.getId());
        return taskMapper.toResponse(task, commentCount);
    }
}
