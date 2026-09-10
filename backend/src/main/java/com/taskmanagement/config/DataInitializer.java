package com.taskmanagement.config;

import com.taskmanagement.entity.*;
import com.taskmanagement.repository.CommentRepository;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           TaskRepository taskRepository,
                           CommentRepository commentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Database is empty. Initializing demo workspace data...");

        // 1. Seed Users
        User admin = userRepository.save(new User(
                null, "Admin User", "admin@example.com",
                passwordEncoder.encode("Password123!"), Role.ADMIN
        ));

        User manager = userRepository.save(new User(
                null, "Sarah Jenkins (Manager)", "manager@example.com",
                passwordEncoder.encode("Password123!"), Role.MANAGER
        ));

        User devUser = userRepository.save(new User(
                null, "Alex Chen (Developer)", "user@example.com",
                passwordEncoder.encode("Password123!"), Role.USER
        ));

        // 2. Seed Projects
        Project project1 = projectRepository.save(new Project(
                null,
                "Salesforce Enterprise CRM Integration",
                "Integrate customer lifecycle workflows, real-time event streaming, and lead scoring pipelines.",
                ProjectStatus.ACTIVE,
                manager
        ));

        Project project2 = projectRepository.save(new Project(
                null,
                "Cloud Migration & Microservices",
                "Decompose legacy monolithic billing modules into scalable, resilient services.",
                ProjectStatus.ACTIVE,
                admin
        ));

        // 3. Seed Tasks
        Task task1 = taskRepository.save(new Task(
                null,
                "Design RESTful Auth & RBAC Architecture",
                "Establish stateless JWT security filters, bcrypt hashing, and role validation for Admin, Manager, User.",
                TaskPriority.CRITICAL,
                TaskStatus.COMPLETED,
                LocalDate.now().plusDays(2),
                project1,
                devUser,
                manager
        ));

        Task task2 = taskRepository.save(new Task(
                null,
                "Implement Task Filter Specification",
                "Build dynamic JPA predicates for status, priority, project, and assignee parameters.",
                TaskPriority.HIGH,
                TaskStatus.IN_PROGRESS,
                LocalDate.now().plusDays(5),
                project1,
                devUser,
                manager
        ));

        Task task3 = taskRepository.save(new Task(
                null,
                "Configure Connection Pooling & Database Indexing",
                "Add composite indexes on foreign keys and frequently queried status/priority columns.",
                TaskPriority.MEDIUM,
                TaskStatus.TODO,
                LocalDate.now().plusDays(10),
                project2,
                devUser,
                admin
        ));

        Task task4 = taskRepository.save(new Task(
                null,
                "External Vendor API Rate Limiting Fix",
                "Mitigate 429 Too Many Requests response by adding exponential backoff.",
                TaskPriority.HIGH,
                TaskStatus.BLOCKED,
                LocalDate.now().plusDays(1),
                project2,
                admin,
                admin
        ));

        // 4. Seed Comments
        commentRepository.save(new Comment(
                null,
                "Completed authentication filter implementation and verified with unit tests.",
                task1,
                devUser
        ));

        commentRepository.save(new Comment(
                null,
                "Looks clean! Ensure all controller endpoints use proper @PreAuthorize annotations.",
                task1,
                manager
        ));

        commentRepository.save(new Comment(
                null,
                "Working on Spring Data JPA Specification query filters now.",
                task2,
                devUser
        ));

        log.info("Demo data initialized successfully!");
        log.info("Default accounts created (Password: Password123!):");
        log.info("  Admin:   admin@example.com");
        log.info("  Manager: manager@example.com");
        log.info("  User:    user@example.com");
    }
}
