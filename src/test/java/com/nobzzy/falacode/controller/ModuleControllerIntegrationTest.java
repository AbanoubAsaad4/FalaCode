package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.ModuleDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ModuleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("POST /api/courses/{courseId}/modules - Should create module successfully")
    void shouldCreateModule() throws Exception {
        Course parentCourse = Course.builder()
                .title("Java Fundamentals")
                .description("Introductory course")
                .isPublished(true)
                .build();

        Course savedCourse = courseRepository.save(parentCourse);

        ModuleDto moduleDto = ModuleDto.builder()
                .title("Module 1")
                .description("Description of Module 1")
                .published(true)
                .build();

        mockMvc.perform(post("/api/courses/{courseId}/modules", savedCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moduleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Module 1"))
                .andExpect(jsonPath("$.description").value("Description of Module 1"))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @DisplayName("GET /api/modules/{id} - Should return 404 when module not found")
    void shouldReturn404_WhenModuleNotFound() throws Exception {
        mockMvc.perform(get("/api/modules/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/courses/{courseId}/modules - Should return 404 when parent course not found")
    void shouldReturn404_WhenParentCourseNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/{courseId}/modules", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/modules/{moduleId}/move-to-course/{courseId} - Should return 404 when course or module not found")
    void shouldReturn404_WhenCourseOrModuleNotFoundOnMove() throws Exception {
        mockMvc.perform(put("/api/modules/{moduleId}/move-to-course/{courseId}", 999L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/courses/{courseId}/modules - Should return list of modules")
    void shouldGetModulesByCourseId() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("Spring Boot Aggregate")
                .description("Course with modules")
                .isPublished(true)
                .build());

        ModuleDto moduleDto = ModuleDto.builder()
                .title("Module A")
                .description("Module A Description")
                .published(true)
                .build();

        mockMvc.perform(post("/api/courses/{courseId}/modules", course.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moduleDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/courses/{courseId}/modules", course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Module A"));
    }

    @Test
    @DisplayName("PUT /api/modules/{moduleId} - Should update module details")
    void shouldUpdateModule() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("Java Core")
                .description("Java base")
                .isPublished(true)
                .build());

        // Create initial module
        String response = mockMvc.perform(post("/api/courses/{courseId}/modules", course.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ModuleDto.builder()
                                .title("Old Module Title")
                                .description("Old Description")
                                .published(false)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long moduleId = objectMapper.readTree(response).get("id").asLong();

        ModuleDto updateDto = ModuleDto.builder()
                .title("Updated Module Title")
                .description("Updated Description")
                .published(true)
                .build();

        mockMvc.perform(put("/api/modules/{moduleId}", moduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Module Title"))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @DisplayName("DELETE /api/modules/{moduleId} - Should delete module successfully")
    void shouldDeleteModule() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("Persistence Tech")
                .description("JPA & Hibernate")
                .isPublished(true)
                .build());

        String response = mockMvc.perform(post("/api/courses/{courseId}/modules", course.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ModuleDto.builder()
                                .title("Temporary Module")
                                .description("To be deleted")
                                .published(true)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long moduleId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/modules/{moduleId}", moduleId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/modules/{id}", moduleId))
                .andExpect(status().isNotFound());
    }
}
