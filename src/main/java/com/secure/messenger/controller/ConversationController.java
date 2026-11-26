
package com.secure.messenger.controller;

import com.secure.messenger.dto.ConversationResponse;
import com.secure.messenger.service.ConversationService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/direct/{recipientId}")
    public ResponseEntity<ConversationResponse> createDirectConversation(
            @PathVariable UUID recipientId) {

        ConversationResponse response =
                conversationService.createDirectConversation(recipientId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ConversationResponse>> getConversations(
            Pageable pageable) {

        return ResponseEntity.ok(
                conversationService.getConversations(pageable));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable UUID conversationId) {

        return ResponseEntity.ok(
                conversationService.getConversation(conversationId));
    }
}
