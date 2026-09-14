package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.ChatSessionDto;
import com.nobzzy.falacode.entity.ChatSession;
import com.nobzzy.falacode.entity.Exercise;
import com.nobzzy.falacode.entity.ChatMessage;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.ChatSessionRepository;
import com.nobzzy.falacode.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ExerciseRepository exerciseRepository;

    public ChatSessionService(ChatSessionRepository chatSessionRepository, ExerciseRepository exerciseRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.exerciseRepository = exerciseRepository;
    }

    // CREATE
    @Transactional
    public ChatSessionDto createChatSession(ChatSessionDto chatSessionDto) {
        Exercise exercise = null;
        if (chatSessionDto.getExerciseId() != null) {
            exercise = exerciseRepository.findById(chatSessionDto.getExerciseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + chatSessionDto.getExerciseId()));
        }

        ChatSession chatSession = mapToEntity(chatSessionDto);
        chatSession.setExercise(exercise);

        ChatSession savedSession = chatSessionRepository.save(chatSession);
        return mapToDto(savedSession);
    }

    // READ ALL
    public List<ChatSessionDto> getAllChatSessions() {
        return chatSessionRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ ALL BY EXERCISE ID
    public List<ChatSessionDto> getChatSessionsByExerciseId(Long exerciseId) {
        if (!exerciseRepository.existsById(exerciseId)) {
            throw new ResourceNotFoundException("Exercise not found with id: " + exerciseId);
        }

        return chatSessionRepository.findByExerciseId(exerciseId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ BY ID
    public ChatSessionDto getChatSessionById(Long id) {
        ChatSession chatSession = chatSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession not found with id: " + id));
        return mapToDto(chatSession);
    }

    // UPDATE
    @Transactional
    public ChatSessionDto updateChatSession(Long id, ChatSessionDto chatSessionDto) {
        ChatSession chatSession = chatSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession not found with id: " + id));

        chatSession.setTitle(chatSessionDto.getTitle());

        ChatSession updatedSession = chatSessionRepository.save(chatSession);
        return mapToDto(updatedSession);
    }

    // DELETE
    @Transactional
    public void deleteChatSessionById(Long id) {
        if (!chatSessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("ChatSession not found with id: " + id);
        }
        chatSessionRepository.deleteById(id);
    }

    ChatSession mapToEntity(ChatSessionDto chatSessionDto) {
        return ChatSession.builder()
                .id(chatSessionDto.getId())
                .title(chatSessionDto.getTitle())
                .build();
    }

    ChatSessionDto mapToDto(ChatSession chatSession) {
        return ChatSessionDto.builder()
                .id(chatSession.getId())
                .title(chatSession.getTitle())
                .exerciseId(chatSession.getExercise() != null ? chatSession.getExercise().getId() : null)
                .messageIds(chatSession.getMessages() != null
                        ? chatSession.getMessages().stream().map(ChatMessage::getId).toList()
                        : List.of())
                .createdAt(chatSession.getCreatedAt())
                .updatedAt(chatSession.getUpdatedAt())
                .build();
    }
}