package com.taskmanagement.service;

import com.taskmanagement.dto.response.DashboardMetricsResponse;
import com.taskmanagement.entity.TaskStatus;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getMetrics() {
        long totalProjects = projectRepository.count();
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.COMPLETED);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long blockedTasks = taskRepository.countByStatus(TaskStatus.BLOCKED);
        long pendingTasks = todoTasks + blockedTasks;

        return new DashboardMetricsResponse(
                totalProjects,
                totalTasks,
                completedTasks,
                inProgressTasks,
                pendingTasks
        );
    }
}
