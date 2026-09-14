package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.ChatSessionDto;
import com.nobzzy.falacode.entity.Course;
import com.nobzzy.falacode.entity.Exercise;
import com.nobzzy.falacode.entity.Lesson;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.entity.ChatMessage;
import com.nobzzy.falacode.entity.ChatSession;
import com.nobzzy.falacode.repository.*;
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
public class ChatSessionControllerIntegrationTest {

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
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private Exercise savedExercise;

    @BeforeEach
    void setUp() {
        Course course = courseRepository.save(Course.builder()
                .title("AI Assisted Coding")
                .isPublished(true)
                .build());

        Module module = moduleRepository.save(Module.builder()
                .title("Module 1: Prompts")
                .isPublished(true)
                .course(course)
                .build());

        Lesson lesson = lessonRepository.save(Lesson.builder()
                .title("Chat Basics")
                .displayOrder(1)
                .isPublished(true)
                .module(module)
                .build());

        savedExercise = exerciseRepository.save(Exercise.builder()
                .title("Fix the Loop")
                .lesson(lesson)
                .build());
    }

    @Test
    @DisplayName("POST /api/exercises/{exerciseId}/chat-sessions - Should create chat session successfully")
    void shouldCreateChatSession() throws Exception {
        ChatSessionDto chatSessionDto = ChatSessionDto.builder()
                .title("Help with Infinite Loop")
                .exerciseId(savedExercise.getId())
                .build();

        mockMvc.perform(post("/api/exercises/{exerciseId}/chat-sessions", savedExercise.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatSessionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Help with Infinite Loop"))
                .andExpect(jsonPath("$.exerciseId").value(savedExercise.getId()));
    }

    @Test
    @DisplayName("DELETE /api/chat-sessions/{id} - Should delete session and cascade delete messages")
    void shouldDeleteChatSessionAndCascadeMessages() throws Exception {
        ChatSession session = chatSessionRepository.save(ChatSession.builder()
                .title("Session to delete")
                .exercise(savedExercise)
                .build());

        ChatMessage message = ChatMessage.builder()
                .content("Hello AI")
                .sender("USER")
                .chatSession(session)
                .build();

        session.getMessages().add(message);
        chatMessageRepository.save(message);

        mockMvc.perform(delete("/api/chat-sessions/{id}", session.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/chat-messages/{id}", message.getId()))
                .andExpect(status().isNotFound());
    }
}