package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.LessonDto;
import com.nobzzy.falacode.entity.Course;
import com.nobzzy.falacode.entity.Exercise;
import com.nobzzy.falacode.entity.Lesson;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.repository.CourseRepository;
import com.nobzzy.falacode.repository.ExerciseRepository;
import com.nobzzy.falacode.repository.LessonRepository;
import com.nobzzy.falacode.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class LessonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    private Module savedModule;

    @BeforeEach
    void setUp() {
        Course course = courseRepository.save(Course.builder()
                .title("Spring Boot & JPA Core")
                .description("Backend Fundamentals")
                .isPublished(true)
                .build());

        savedModule = moduleRepository.save(Module.builder()
                .title("Module 1: Getting Started")
                .description("Introductory Module")
                .isPublished(true)
                .course(course)
                .build());
    }

    @Test
    @DisplayName("POST /api/lessons - Should create lesson successfully")
    void shouldCreateLesson() throws Exception {
        LessonDto lessonDto = LessonDto.builder()
                .title("Introduction to Controllers")
                .displayOrder(1)
                .published(true)
                .moduleId(savedModule.getId())
                .build();

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Introduction to Controllers"))
                .andExpect(jsonPath("$.displayOrder").value(1))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @DisplayName("GET /api/lessons/{id} - Should return lesson when ID exists")
    void shouldGetLessonById() throws Exception {
        LessonDto lessonDto = LessonDto.builder()
                .title("Database Persistence with JPA")
                .displayOrder(2)
                .published(true)
                .moduleId(savedModule.getId())
                .build();

        String response = mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long lessonId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/lessons/{id}", lessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonId))
                .andExpect(jsonPath("$.title").value("Database Persistence with JPA"));
    }

    @Test
    @DisplayName("GET /api/lessons/{id} - Should return 404 when lesson not found")
    void shouldReturn404_WhenLessonNotFound() throws Exception {
        mockMvc.perform(get("/api/lessons/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/modules/{moduleId}/lessons - Should return lessons for module")
    void shouldGetLessonsByModuleId() throws Exception {
        LessonDto lessonDto = LessonDto.builder()
                .title("REST API Best Practices")
                .displayOrder(1)
                .published(true)
                .moduleId(savedModule.getId())
                .build();

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/modules/{moduleId}/lessons", savedModule.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("REST API Best Practices"));
    }

    @Test
    @DisplayName("PUT /api/lessons/{id} - Should update lesson details")
    void shouldUpdateLesson() throws Exception {
        LessonDto initialDto = LessonDto.builder()
                .title("Old Lesson Title")
                .displayOrder(1)
                .published(false)
                .moduleId(savedModule.getId())
                .build();

        String response = mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long lessonId = objectMapper.readTree(response).get("id").asLong();

        LessonDto updateDto = LessonDto.builder()
                .title("Updated Lesson Title")
                .displayOrder(2)
                .published(true)
                .moduleId(savedModule.getId())
                .build();

        mockMvc.perform(put("/api/lessons/{id}", lessonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Lesson Title"))
                .andExpect(jsonPath("$.displayOrder").value(2))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @DisplayName("DELETE /api/lessons/{id} - Should delete lesson successfully")
    void shouldDeleteLesson() throws Exception {
        LessonDto lessonDto = LessonDto.builder()
                .title("Temporary Lesson")
                .displayOrder(1)
                .published(true)
                .moduleId(savedModule.getId())
                .build();

        String response = mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long lessonId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/lessons/{id}", lessonId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/lessons/{id}", lessonId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/lessons/{id} - Should delete lesson and associated exercises")
    void shouldDeleteLessonAndAssociatedExercises() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("Parent Course")
                .isPublished(true)
                .build());

        Module module = moduleRepository.save(Module.builder()
                .title("Module with lessons")
                .isPublished(false)
                .course(course)
                .build());

        Lesson lesson = lessonRepository.save(Lesson.builder()
                .title("Lesson with exercises")
                .isPublished(false)
                .module(module)
                .build());

        Exercise exercise = Exercise.builder()
                .title("New exercise")
                .lesson(lesson)
                .build();

        lesson.getExercises().add(exercise);
        exerciseRepository.save(exercise);

        mockMvc.perform(delete("/api/lessons/{id}", lesson.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/exercises/{id}", exercise.getId()))
                .andExpect(status().isNotFound());
    }
}