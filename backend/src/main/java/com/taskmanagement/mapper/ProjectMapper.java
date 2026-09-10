package com.taskmanagement.mapper;

import com.taskmanagement.dto.request.ProjectRequest;
import com.taskmanagement.dto.response.ProjectResponse;
import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.ProjectStatus;
import com.taskmanagement.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    private final UserMapper userMapper;

    public ProjectMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Project toEntity(ProjectRequest request, User creator) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.ACTIVE);
        project.setCreatedBy(creator);
        return project;
    }

    public ProjectResponse toResponse(Project project, long totalTasks, long completedTasks) {
        if (project == null) {
            return null;
        }
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                userMapper.toResponse(project.getCreatedBy()),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                totalTasks,
                completedTasks
        );
    }
}
