package com.nobzzy.falacode.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import com.nobzzy.falacode.dto.LessonDto;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class LessonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LessonService lessonService;

    private LessonDto sampleLessonDto;

    @BeforeEach
    void setUp() {
        sampleLessonDto = LessonDto.builder()
                .id(1L)
                .title("Introduction to Spring Boot")
                .displayOrder(1)
                .published(true)
                .moduleId(10L)
                .build();
    }

    @Nested
    @DisplayName("POST /api/lessons")
    class CreateLessonEndpoint {

        @Test
        @DisplayName("Should create lesson and return 201 Created")
        void shouldCreateLesson_Returns201() throws Exception {
            when(lessonService.createLesson(any(LessonDto.class))).thenReturn(sampleLessonDto);

            mockMvc.perform(post("/api/lessons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleLessonDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Introduction to Spring Boot"))
                    .andExpect(jsonPath("$.moduleId").value(10L));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when title is blank")
        void shouldReturn400_WhenTitleIsBlank() throws Exception {
            LessonDto invalidDto = LessonDto.builder()
                    .title("")
                    .moduleId(10L)
                    .build();

            mockMvc.perform(post("/api/lessons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(lessonService, never()).createLesson(any());
        }
    }

    @Nested
    @DisplayName("GET /api/lessons")
    class ReadLessonsEndpoints {

        @Test
        @DisplayName("GET /api/lessons - Should return list of lessons with 200 OK")
        void shouldGetAllLessons_Returns200() throws Exception {
            when(lessonService.getAllLessons()).thenReturn(List.of(sampleLessonDto));

            mockMvc.perform(get("/api/lessons"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("Introduction to Spring Boot"));
        }

        @Test
        @DisplayName("GET /api/modules/{moduleId}/lessons - Should return lessons for module")
        void shouldGetLessonsByModuleId_Returns200() throws Exception {
            when(lessonService.getLessonsByModuleId(10L)).thenReturn(List.of(sampleLessonDto));

            mockMvc.perform(get("/api/modules/10/lessons"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].moduleId").value(10L));
        }

        @Test
        @DisplayName("GET /api/lessons/{id} - Should return lesson when ID exists")
        void shouldGetLessonById_Returns200() throws Exception {
            when(lessonService.getLessonById(1L)).thenReturn(sampleLessonDto);

            mockMvc.perform(get("/api/lessons/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Introduction to Spring Boot"));
        }

        @Test
        @DisplayName("GET /api/lessons/{id} - Should return 404 Not Found when ID does not exist")
        void shouldReturn404_WhenLessonNotFound() throws Exception {
            when(lessonService.getLessonById(99L))
                    .thenThrow(new ResourceNotFoundException("Lesson not found with id: 99"));

            mockMvc.perform(get("/api/lessons/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/lessons/{id}")
    class UpdateLessonEndpoint {

        @Test
        @DisplayName("Should update lesson and return 200 OK")
        void shouldUpdateLesson_Returns200() throws Exception {
            LessonDto updatedDto = LessonDto.builder()
                    .id(1L)
                    .title("Updated Lesson Title")
                    .displayOrder(2)
                    .published(false)
                    .moduleId(10L)
                    .build();

            when(lessonService.updateLesson(eq(1L), any(LessonDto.class))).thenReturn(updatedDto);

            mockMvc.perform(put("/api/lessons/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Lesson Title"))
                    .andExpect(jsonPath("$.displayOrder").value(2))
                    .andExpect(jsonPath("$.published").value(false));
        }
    }

    @Nested
    @DisplayName("DELETE /api/lessons/{id}")
    class DeleteLessonEndpoint {

        @Test
        @DisplayName("Should delete lesson and return 204 No Content")
        void shouldDeleteLesson_Returns204() throws Exception {
            doNothing().when(lessonService).deleteLessonById(1L);

            mockMvc.perform(delete("/api/lessons/1"))
                    .andExpect(status().isNoContent());

            verify(lessonService, times(1)).deleteLessonById(1L);
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existent lesson")
        void shouldReturn404_WhenDeletingNonExistentLesson() throws Exception {
            doThrow(new ResourceNotFoundException("Lesson not found with id: 99"))
                    .when(lessonService).deleteLessonById(99L);

            mockMvc.perform(delete("/api/lessons/99"))
                    .andExpect(status().isNotFound());
        }
    }
}