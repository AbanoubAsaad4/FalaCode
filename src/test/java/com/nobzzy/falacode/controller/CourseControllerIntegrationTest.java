package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.CourseDto;
import com.nobzzy.falacode.entity.Course;
import com.nobzzy.falacode.repository.CourseRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("POST /api/courses - Should create course successfully")
    void shouldCreateCourse() throws Exception {
        CourseDto courseDto = CourseDto.builder()
                .title("Java Fundamentals")
                .description("Introductory course")
                .published(true)
                .build();

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Java Fundamentals"))
                .andExpect(jsonPath("$.description").value("Introductory course"))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @DisplayName("GET /api/courses - Should return all courses")
    void shouldGetAllCourses() throws Exception {
        Course course = Course.builder()
                .title("Spring Boot Deep Dive")
                .description("Advanced Spring")
                .isPublished(true)
                .build();
        courseRepository.save(course);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Spring Boot Deep Dive"));
    }

    @Test
    @DisplayName("GET /api/courses/{id} - Should return course when exists")
    void shouldGetCourseById() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("SQL Basics")
                .description("Relational Databases")
                .isPublished(true)
                .build());

        mockMvc.perform(get("/api/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(course.getId()))
                .andExpect(jsonPath("$.title").value("SQL Basics"));
    }

    @Test
    @DisplayName("GET /api/courses/{id} - Should return 404 when course not found")
    void shouldReturn404_WhenCourseNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/courses/{id} - Should update course successfully")
    void shouldUpdateCourse() throws Exception {
        Course savedCourse = courseRepository.save(Course.builder()
                .title("Old Title")
                .description("Old Description")
                .isPublished(false)
                .build());

        CourseDto updateDto = CourseDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .published(true)
                .build();

        mockMvc.perform(put("/api/courses/{id}", savedCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} - Should delete course successfully")
    void shouldDeleteCourse() throws Exception {
        Course savedCourse = courseRepository.save(Course.builder()
                .title("Course to Delete")
                .description("Will be removed")
                .isPublished(false)
                .build());

        mockMvc.perform(delete("/api/courses/{id}", savedCourse.getId()))
                .andExpect(status().isNoContent());

        // Verify entity no longer exists
        mockMvc.perform(get("/api/courses/{id}", savedCourse.getId()))
                .andExpect(status().isNotFound());
    }
}