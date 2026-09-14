package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.ChatSessionDto;
import com.nobzzy.falacode.entity.ChatSession;
import com.nobzzy.falacode.entity.Exercise;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.ChatSessionRepository;
import com.nobzzy.falacode.repository.ExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatSessionServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ChatSessionService chatSessionService;

    private Exercise sampleExercise;
    private ChatSession sampleChatSession;
    private ChatSessionDto sampleChatSessionDto;

    @BeforeEach
    void setUp() {
        sampleExercise = Exercise.builder()
                .id(30L)
                .title("Recursion Practice")
                .build();

        sampleChatSession = ChatSession.builder()
                .id(300L)
                .title("Debugging StackOverflowError")
                .exercise(sampleExercise)
                .build();

        sampleChatSessionDto = ChatSessionDto.builder()
                .id(300L)
                .title("Debugging StackOverflowError")
                .exerciseId(30L)
                .build();
    }

    @Nested
    @DisplayName("Create ChatSession Tests")
    class CreateChatSessionTests {

        @Test
        @DisplayName("Should create and return ChatSessionDto when exercise exists")
        void createChatSession_Success() {
            when(exerciseRepository.findById(30L)).thenReturn(Optional.of(sampleExercise));
            when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(sampleChatSession);

            ChatSessionDto result = chatSessionService.createChatSession(sampleChatSessionDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(300L);
            assertThat(result.getTitle()).isEqualTo("Debugging StackOverflowError");
            assertThat(result.getExerciseId()).isEqualTo(30L);
            verify(chatSessionRepository, times(1)).save(any(ChatSession.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when exercise does not exist")
        void createChatSession_ThrowsException_WhenExerciseNotFound() {
            when(exerciseRepository.findById(30L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chatSessionService.createChatSession(sampleChatSessionDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Exercise not found with id: 30");

            verify(chatSessionRepository, never()).save(any(ChatSession.class));
        }
    }

    @Nested
    @DisplayName("Read ChatSession Tests")
    class ReadChatSessionTests {

        @Test
        @DisplayName("Should return all chat sessions")
        void getAllChatSessions_Success() {
            when(chatSessionRepository.findAll()).thenReturn(List.of(sampleChatSession));

            List<ChatSessionDto> results = chatSessionService.getAllChatSessions();

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getTitle()).isEqualTo("Debugging StackOverflowError");
        }

        @Test
        @DisplayName("Should return chat session by ID when exists")
        void getChatSessionById_Success() {
            when(chatSessionRepository.findById(300L)).thenReturn(Optional.of(sampleChatSession));

            ChatSessionDto result = chatSessionService.getChatSessionById(300L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(300L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when chat session ID does not exist")
        void getChatSessionById_ThrowsException_WhenNotFound() {
            when(chatSessionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chatSessionService.getChatSessionById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ChatSession not found with id: 999");
        }
    }

    @Nested
    @DisplayName("Delete ChatSession Tests")
    class DeleteChatSessionTests {

        @Test
        @DisplayName("Should delete chat session when ID exists")
        void deleteChatSessionById_Success() {
            when(chatSessionRepository.existsById(300L)).thenReturn(true);

            chatSessionService.deleteChatSessionById(300L);

            verify(chatSessionRepository, times(1)).deleteById(300L);
        }
    }
}