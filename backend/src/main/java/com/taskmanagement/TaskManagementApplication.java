package com.taskmanagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskManagementApplication {

    private static final Logger log = LoggerFactory.getLogger(TaskManagementApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
        log.info("Task Management & Collaboration Platform successfully started!");
    }
}
