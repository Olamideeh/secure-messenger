
package com.secure.messenger.controller;

import com.secure.messenger.dto.ContactRequestResponse;
import com.secure.messenger.dto.SendContactRequest;
import com.secure.messenger.service.ContactRequestService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contact-requests")
public class ContactRequestController {

    private final ContactRequestService contactRequestService;

    public ContactRequestController(ContactRequestService contactRequestService) {
        this.contactRequestService = contactRequestService;
    }

    @PostMapping
    public ResponseEntity<ContactRequestResponse> sendRequest(
            @Valid @RequestBody SendContactRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactRequestService.createContactRequest(request));
    }

    @GetMapping("/incoming")
    public ResponseEntity<Page<ContactRequestResponse>> getIncomingRequests(
            Pageable pageable) {

        return ResponseEntity.ok(
                contactRequestService.getIncomingRequests(pageable));
    }

    @GetMapping("/outgoing")
    public ResponseEntity<Page<ContactRequestResponse>> getOutgoingRequests(
            Pageable pageable) {

        return ResponseEntity.ok(
                contactRequestService.getOutgoingRequests(pageable));
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ContactRequestResponse> acceptRequest(
            @PathVariable UUID requestId) {

        return ResponseEntity.ok(
                contactRequestService.acceptContactRequest(requestId));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ContactRequestResponse> rejectRequest(
            @PathVariable UUID requestId) {

        return ResponseEntity.ok(
                contactRequestService.rejectContactRequest(requestId));
    }
}
