package com.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.request.ProjectRequest;
import com.taskmanagement.dto.response.ProjectResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.ProjectStatus;
import com.taskmanagement.entity.Role;
import com.taskmanagement.exception.ProjectNotFoundException;
import com.taskmanagement.security.CustomUserDetailsService;
import com.taskmanagement.security.JwtAuthenticationEntryPoint;
import com.taskmanagement.security.JwtTokenProvider;
import com.taskmanagement.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("POST /api/projects with valid request returns 201 CREATED")
    void testCreateProject_Success() throws Exception {
        ProjectRequest request = new ProjectRequest("Cloud Migration", "Migrate microservices", ProjectStatus.ACTIVE);
        ProjectResponse response = new ProjectResponse(
                1L, "Cloud Migration", "Migrate microservices", ProjectStatus.ACTIVE,
                new UserResponse(2L, "Manager", "mgr@example.com", Role.MANAGER, null, null),
                null, null, 0, 0
        );

        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Cloud Migration"));
    }

    @Test
    @DisplayName("GET /api/projects returns list of projects")
    void testGetAllProjects_Success() throws Exception {
        ProjectResponse response = new ProjectResponse(
                1L, "Cloud Migration", "Migrate microservices", ProjectStatus.ACTIVE,
                new UserResponse(2L, "Manager", "mgr@example.com", Role.MANAGER, null, null),
                null, null, 5, 2
        );

        when(projectService.getAllProjects()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].totalTasks").value(5))
                .andExpect(jsonPath("$[0].completedTasks").value(2));
    }

    @Test
    @DisplayName("GET /api/projects/{id} when not found returns 404 NOT FOUND")
    void testGetProjectById_NotFound() throws Exception {
        when(projectService.getProjectById(99L))
                .thenThrow(new ProjectNotFoundException("Project not found with id: 99"));

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found with id: 99"));
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} returns 204 NO CONTENT")
    void testDeleteProject_Success() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());
    }
}
