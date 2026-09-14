package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.ChatSessionDto;
import com.nobzzy.falacode.service.ChatSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    // CREATE
    @PostMapping("/chat-sessions")
    public ResponseEntity<ChatSessionDto> createChatSession(@Valid @RequestBody ChatSessionDto chatSessionDto) {
        return new ResponseEntity<>(chatSessionService.createChatSession(chatSessionDto), HttpStatus.CREATED);
    }

    // READ ALL
    @GetMapping("/chat-sessions")
    public ResponseEntity<List<ChatSessionDto>> getAllChatSessions() {
        return ResponseEntity.ok(chatSessionService.getAllChatSessions());
    }

    // READ ALL BY EXERCISE ID
    @GetMapping("/exercises/{exerciseId}/chat-sessions")
    public ResponseEntity<List<ChatSessionDto>> getChatSessionsByExerciseId(@PathVariable Long exerciseId) {
        return ResponseEntity.ok(chatSessionService.getChatSessionsByExerciseId(exerciseId));
    }

    // READ BY ID
    @GetMapping("/chat-sessions/{id}")
    public ResponseEntity<ChatSessionDto> getChatSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(chatSessionService.getChatSessionById(id));
    }

    // UPDATE
    @PutMapping("/chat-sessions/{id}")
    public ResponseEntity<ChatSessionDto> updateChatSession(@Valid @RequestBody ChatSessionDto chatSessionDto, @PathVariable Long id) {
        return ResponseEntity.ok(chatSessionService.updateChatSession(id, chatSessionDto));
    }

    // DELETE
    @DeleteMapping("/chat-sessions/{id}")
    public ResponseEntity<Void> deleteChatSession(@PathVariable Long id) {
        chatSessionService.deleteChatSessionById(id);
        return ResponseEntity.noContent().build();
    }
}