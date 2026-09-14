package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.SubmissionDto;
import com.nobzzy.falacode.entity.Course;
import com.nobzzy.falacode.entity.Exercise;
import com.nobzzy.falacode.entity.Lesson;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.repository.CourseRepository;
import com.nobzzy.falacode.repository.ExerciseRepository;
import com.nobzzy.falacode.repository.LessonRepository;
import com.nobzzy.falacode.repository.ModuleRepository;
import com.nobzzy.falacode.repository.SubmissionRepository;
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
public class SubmissionControllerIntegrationTest {

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

    @Autowired
    private SubmissionRepository submissionRepository;

    private Exercise savedExercise;

    @BeforeEach
    void setUp() {
        Course course = courseRepository.save(Course.builder()
                .title("Java Mastery")
                .description("Advanced Java")
                .isPublished(true)
                .build());

        Module module = moduleRepository.save(Module.builder()
                .title("Module 1: Algorithms")
                .description("Data Structures and Algorithms")
                .isPublished(true)
                .course(course)
                .build());

        Lesson lesson = lessonRepository.save(Lesson.builder()
                .title("Arrays & Strings")
                .displayOrder(1)
                .isPublished(true)
                .module(module)
                .build());

        savedExercise = exerciseRepository.save(Exercise.builder()
                .title("Reverse String")
                .instructions("Write a function to reverse a string")
                .lesson(lesson)
                .build());
    }

    @Test
    @DisplayName("POST /api/submissions - Should create submission successfully")
    void shouldCreateSubmission() throws Exception {
        SubmissionDto submissionDto = SubmissionDto.builder()
                .code("public String reverse(String s) { return new StringBuilder(s).reverse().toString(); }")
                .exerciseId(savedExercise.getId())
                .build();

        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("public String reverse(String s) { return new StringBuilder(s).reverse().toString(); }"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/submissions/{id} - Should return submission when ID exists")
    void shouldGetSubmissionById() throws Exception {
        SubmissionDto submissionDto = SubmissionDto.builder()
                .code("System.out.println(\"Hello World\");")
                .exerciseId(savedExercise.getId())
                .build();

        String response = mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long submissionId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/submissions/{id}", submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId))
                .andExpect(jsonPath("$.code").value("System.out.println(\"Hello World\");"));
    }

    @Test
    @DisplayName("GET /api/submissions/{id} - Should return 404 when submission not found")
    void shouldReturn404_WhenSubmissionNotFound() throws Exception {
        mockMvc.perform(get("/api/submissions/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/exercises/{exerciseId}/submissions - Should return submissions for exercise")
    void shouldGetSubmissionsByExerciseId() throws Exception {
        SubmissionDto submissionDto = SubmissionDto.builder()
                .code("int a = 5;")
                .exerciseId(savedExercise.getId())
                .build();

        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/exercises/{exerciseId}/submissions", savedExercise.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("int a = 5;"));
    }

    @Test
    @DisplayName("PUT /api/submissions/{id} - Should update submission details")
    void shouldUpdateSubmission() throws Exception {
        SubmissionDto initialDto = SubmissionDto.builder()
                .code("int x = 10;")
                .exerciseId(savedExercise.getId())
                .build();

        String response = mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long submissionId = objectMapper.readTree(response).get("id").asLong();

        SubmissionDto updateDto = SubmissionDto.builder()
                .code("int x = 20;")
                .status("PASSED")
                .score(100)
                .feedback("Perfect solution")
                .exerciseId(savedExercise.getId())
                .build();

        mockMvc.perform(put("/api/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("int x = 20;"))
                .andExpect(jsonPath("$.status").value("PASSED"))
                .andExpect(jsonPath("$.score").value(100))
                .andExpect(jsonPath("$.feedback").value("Perfect solution"));
    }

    @Test
    @DisplayName("DELETE /api/submissions/{id} - Should delete submission successfully")
    void shouldDeleteSubmission() throws Exception {
        SubmissionDto submissionDto = SubmissionDto.builder()
                .code("String s = \"temp\";")
                .exerciseId(savedExercise.getId())
                .build();

        String response = mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long submissionId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/submissions/{id}", submissionId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/submissions/{id}", submissionId))
                .andExpect(status().isNotFound());
    }
}