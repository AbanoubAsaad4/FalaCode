package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.ChatMessageDto;
import com.nobzzy.falacode.entity.ChatMessage;
import com.nobzzy.falacode.entity.ChatSession;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.ChatMessageRepository;
import com.nobzzy.falacode.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, ChatSessionRepository chatSessionRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatSessionRepository = chatSessionRepository;
    }

    // CREATE
    @Transactional
    public ChatMessageDto createChatMessage(ChatMessageDto chatMessageDto) {
        ChatSession chatSession = chatSessionRepository.findById(chatMessageDto.getChatSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession not found with id: " + chatMessageDto.getChatSessionId()));

        ChatMessage chatMessage = mapToEntity(chatMessageDto);
        chatMessage.setChatSession(chatSession);

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return mapToDto(savedMessage);
    }

    // READ ALL BY CHAT SESSION ID
    public List<ChatMessageDto> getMessagesByChatSessionId(Long chatSessionId) {
        if (!chatSessionRepository.existsById(chatSessionId)) {
            throw new ResourceNotFoundException("ChatSession not found with id: " + chatSessionId);
        }

        return chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(chatSessionId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ BY ID
    public ChatMessageDto getChatMessageById(Long id) {
        ChatMessage chatMessage = chatMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage not found with id: " + id));
        return mapToDto(chatMessage);
    }

    // UPDATE
    @Transactional
    public ChatMessageDto updateChatMessage(Long id, ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = chatMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage not found with id: " + id));

        chatMessage.setContent(chatMessageDto.getContent());
        chatMessage.setSender(chatMessageDto.getSender());

        ChatMessage updatedMessage = chatMessageRepository.save(chatMessage);
        return mapToDto(updatedMessage);
    }

    // DELETE
    @Transactional
    public void deleteChatMessageById(Long id) {
        if (!chatMessageRepository.existsById(id)) {
            throw new ResourceNotFoundException("ChatMessage not found with id: " + id);
        }
        chatMessageRepository.deleteById(id);
    }

    ChatMessage mapToEntity(ChatMessageDto chatMessageDto) {
        return ChatMessage.builder()
                .id(chatMessageDto.getId())
                .content(chatMessageDto.getContent())
                .sender(chatMessageDto.getSender())
                .build();
    }

    ChatMessageDto mapToDto(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .content(chatMessage.getContent())
                .sender(chatMessage.getSender())
                .chatSessionId(chatMessage.getChatSession() != null ? chatMessage.getChatSession().getId() : null)
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}