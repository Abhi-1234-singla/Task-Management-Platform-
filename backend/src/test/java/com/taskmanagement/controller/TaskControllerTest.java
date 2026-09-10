package com.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.request.TaskRequest;
import com.taskmanagement.dto.request.TaskStatusUpdateRequest;
import com.taskmanagement.dto.response.PagedResponse;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.TaskPriority;
import com.taskmanagement.entity.TaskStatus;
import com.taskmanagement.security.CustomUserDetailsService;
import com.taskmanagement.security.JwtAuthenticationEntryPoint;
import com.taskmanagement.security.JwtTokenProvider;
import com.taskmanagement.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("POST /api/tasks with valid body returns 201 CREATED")
    void testCreateTask_Success() throws Exception {
        TaskRequest request = new TaskRequest(
                "Write Unit Tests", "High coverage", TaskPriority.HIGH, TaskStatus.TODO,
                LocalDate.now().plusDays(3), 1L, 2L
        );
        TaskResponse response = new TaskResponse(
                10L, "Write Unit Tests", "High coverage", TaskPriority.HIGH, TaskStatus.TODO,
                LocalDate.now().plusDays(3), 1L, "Platform",
                new UserResponse(2L, "Developer", "dev@example.com", Role.USER, null, null),
                new UserResponse(1L, "Manager", "mgr@example.com", Role.MANAGER, null, null),
                null, null, 0
        );

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Write Unit Tests"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @DisplayName("POST /api/tasks with blank title returns 400 BAD REQUEST")
    void testCreateTask_BlankTitle_ReturnsBadRequest() throws Exception {
        TaskRequest request = new TaskRequest(
                "", "High coverage", TaskPriority.HIGH, TaskStatus.TODO,
                LocalDate.now().plusDays(3), 1L, 2L
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("GET /api/tasks with status and priority filtering returns 200 OK")
    void testGetTasks_WithFilters() throws Exception {
        TaskResponse response = new TaskResponse(
                10L, "Write Unit Tests", "High coverage", TaskPriority.HIGH, TaskStatus.IN_PROGRESS,
                LocalDate.now().plusDays(3), 1L, "Platform", null, null, null, null, 0
        );
        PagedResponse<TaskResponse> pagedResponse = new PagedResponse<>(List.of(response), 0, 10, 1, 1, true);

        when(taskService.getTasks(eq(TaskStatus.IN_PROGRESS), eq(TaskPriority.HIGH), any(), any(), eq(0), eq(10), anyString(), anyString()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "IN_PROGRESS")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L))
                .andExpect(jsonPath("$.content[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status updates status and returns 200 OK")
    void testUpdateTaskStatus_Success() throws Exception {
        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.COMPLETED);
        TaskResponse response = new TaskResponse(
                10L, "Write Unit Tests", "High coverage", TaskPriority.HIGH, TaskStatus.COMPLETED,
                LocalDate.now().plusDays(3), 1L, "Platform", null, null, null, null, 0
        );

        when(taskService.updateTaskStatus(eq(10L), eq(TaskStatus.COMPLETED))).thenReturn(response);

        mockMvc.perform(patch("/api/tasks/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} returns 204 NO CONTENT")
    void testDeleteTask_Success() throws Exception {
        doNothing().when(taskService).deleteTask(10L);

        mockMvc.perform(delete("/api/tasks/10"))
                .andExpect(status().isNoContent());
    }
}
