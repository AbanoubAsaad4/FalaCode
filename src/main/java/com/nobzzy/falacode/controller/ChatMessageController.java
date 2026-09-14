package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.ChatMessageDto;
import com.nobzzy.falacode.service.ChatMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    // CREATE
    @PostMapping("/chat-messages")
    public ResponseEntity<ChatMessageDto> createChatMessage(@Valid @RequestBody ChatMessageDto chatMessageDto) {
        return new ResponseEntity<>(chatMessageService.createChatMessage(chatMessageDto), HttpStatus.CREATED);
    }

    // READ ALL BY CHAT SESSION ID
    @GetMapping("/chat-sessions/{chatSessionId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessagesByChatSessionId(@PathVariable Long chatSessionId) {
        return ResponseEntity.ok(chatMessageService.getMessagesByChatSessionId(chatSessionId));
    }

    // READ BY ID
    @GetMapping("/chat-messages/{id}")
    public ResponseEntity<ChatMessageDto> getChatMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(chatMessageService.getChatMessageById(id));
    }

    // UPDATE
    @PutMapping("/chat-messages/{id}")
    public ResponseEntity<ChatMessageDto> updateChatMessage(@Valid @RequestBody ChatMessageDto chatMessageDto, @PathVariable Long id) {
        return ResponseEntity.ok(chatMessageService.updateChatMessage(id, chatMessageDto));
    }

    // DELETE
    @DeleteMapping("/chat-messages/{id}")
    public ResponseEntity<Void> deleteChatMessage(@PathVariable Long id) {
        chatMessageService.deleteChatMessageById(id);
        return ResponseEntity.noContent().build();
    }
}