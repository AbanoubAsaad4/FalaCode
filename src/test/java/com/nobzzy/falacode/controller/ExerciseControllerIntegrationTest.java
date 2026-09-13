package com.nobzzy.falacode.controller;

import tools.jackson.databind.ObjectMapper;
import com.nobzzy.falacode.dto.ExerciseDto;
import com.nobzzy.falacode.entity.Course;
import com.nobzzy.falacode.entity.Lesson;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.repository.CourseRepository;
import com.nobzzy.falacode.repository.ExerciseRepository;
import com.nobzzy.falacode.repository.LessonRepository;
import com.nobzzy.falacode.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ExerciseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        exerciseRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
        courseRepository.deleteAll();

        Course course = courseRepository.save(Course.builder()
                .title("Java Fundamentals")
                .description("Learn basic Java")
                .build());

        Module module = moduleRepository.save(Module.builder()
                .title("Object-Oriented Programming")
                .description("OOP Basics")
                .displayOrder(1)
                .course(course)
                .build());

        testLesson = lessonRepository.save(Lesson.builder()
                .title("Classes & Objects")
                .displayOrder(1)
                .module(module)
                .build());
    }

    @Nested
    @DisplayName("POST /api/exercises")
    class CreateExerciseTests {

        @Test
        @DisplayName("Should create exercise successfully when payload is valid")
        void shouldCreateExercise_WhenValidRequest() throws Exception {
            ExerciseDto requestDto = ExerciseDto.builder()
                    .title("Create a Car Class")
                    .instructions("Define a class named Car with color field.")
                    .starterCode("public class Car {}")
                    .difficulty("EASY")
                    .displayOrder(1)
                    .points(10)
                    .lessonId(testLesson.getId())
                    .build();

            mockMvc.perform(post("/api/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.title", is("Create a Car Class")))
                    .andExpect(jsonPath("$.instructions", is("Define a class named Car with color field.")))
                    .andExpect(jsonPath("$.starterCode", is("public class Car {}")))
                    .andExpect(jsonPath("$.difficulty", is("EASY")))
                    .andExpect(jsonPath("$.displayOrder", is(1)))
                    .andExpect(jsonPath("$.points", is(10)))
                    .andExpect(jsonPath("$.lessonId", is(testLesson.getId().intValue())));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when title is blank")
        void shouldReturn400_WhenTitleIsBlank() throws Exception {
            ExerciseDto requestDto = ExerciseDto.builder()
                    .title("")
                    .lessonId(testLesson.getId())
                    .build();

            mockMvc.perform(post("/api/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 Not Found when lessonId does not exist")
        void shouldReturn404_WhenLessonDoesNotExist() throws Exception {
            ExerciseDto requestDto = ExerciseDto.builder()
                    .title("Valid Exercise Title")
                    .lessonId(999L)
                    .build();

            mockMvc.perform(post("/api/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/exercises and GET /api/exercises/{id}")
    class ReadExerciseTests {

        @Test
        @DisplayName("Should return all exercises")
        void shouldReturnAllExercises() throws Exception {
            ExerciseDto ex1 = ExerciseDto.builder()
                    .title("Exercise 1")
                    .lessonId(testLesson.getId())
                    .build();

            ExerciseDto ex2 = ExerciseDto.builder()
                    .title("Exercise 2")
                    .lessonId(testLesson.getId())
                    .build();

            mockMvc.perform(post("/api/exercises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ex1)));

            mockMvc.perform(post("/api/exercises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ex2)));

            mockMvc.perform(get("/api/exercises"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(2)))
                    .andExpect(jsonPath("$[0].title", is("Exercise 1")))
                    .andExpect(jsonPath("$[1].title", is("Exercise 2")));
        }

        @Test
        @DisplayName("Should return exercise by ID")
        void shouldReturnExerciseById() throws Exception {
            ExerciseDto requestDto = ExerciseDto.builder()
                    .title("Single Exercise")
                    .lessonId(testLesson.getId())
                    .build();

            String response = mockMvc.perform(post("/api/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ExerciseDto created = objectMapper.readValue(response, ExerciseDto.class);

            mockMvc.perform(get("/api/exercises/{id}", created.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                    .andExpect(jsonPath("$.title", is("Single Exercise")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when getting non-existent exercise ID")
        void shouldReturn404_WhenExerciseNotFound() throws Exception {
            mockMvc.perform(get("/api/exercises/{id}", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return exercises by lesson ID")
        void shouldReturnExercisesByLessonId() throws Exception {
            ExerciseDto ex = ExerciseDto.builder()
                    .title("Lesson Exercise")
                    .lessonId(testLesson.getId())
                    .build();

            mockMvc.perform(post("/api/exercises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ex)));

            mockMvc.perform(get("/api/lessons/{lessonId}/exercises", testLesson.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(1)))
                    .andExpect(jsonPath("$[0].title", is("Lesson Exercise")));
        }
    }

    @Nested
    @DisplayName("PUT /api/exercises/{id}")
    class UpdateExerciseTests {

        @Test
        @DisplayName("Should update exercise successfully")
        void shouldUpdateExercise() throws Exception {
            ExerciseDto initialDto = ExerciseDto.builder()
                    .title("Old Title")
                    .starterCode("int x = 0;")
                    .lessonId(testLesson.getId())
                    .build();

            String response = mockMvc.perform(post("/api/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(initialDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ExerciseDto created = objectMapper.readValue(response, ExerciseDto.class);

            ExerciseDto updateDto = ExerciseDto.builder()
                    .title("Updated Title")
                    .starterCode("int x = 10;")
                    .difficulty("HARD")
                    .lessonId(testLesson.getId())
                    .build();

            mockMvc.perform(put("/api/exercises/{id}", created.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                    .andExpect(jsonPath("$.title", is("Updated Title")))
                    .andExpect(jsonPath("$.starterCode", is("int x = 10;")))
                    .andExpect(jsonPath("$.difficulty", is("HARD")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/exercises/{id}")
    class DeleteExerciseTests {

        @Test
        @DisplayName("Should delete exercise successfully")
        void shouldDeleteExercise() throws Exception {
            ExerciseDto requestDto = ExerciseDto.builder()
                    .title("To Be Deleted")
                    .lessonId(testLesson.getId())
                    .build();

            String response = mockMvc.perform(post("/api/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ExerciseDto created = objectMapper.readValue(response, ExerciseDto.class);

            mockMvc.perform(delete("/api/exercises/{id}", created.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/exercises/{id}", created.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existent exercise ID")
        void shouldReturn404_WhenDeletingNonExistentExercise() throws Exception {
            mockMvc.perform(delete("/api/exercises/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }
}