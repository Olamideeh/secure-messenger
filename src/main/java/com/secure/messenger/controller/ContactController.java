
package com.secure.messenger.controller;

import com.secure.messenger.dto.ContactResponse;
import com.secure.messenger.service.ContactService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<Page<ContactResponse>> getContacts(
            Pageable pageable) {

        return ResponseEntity.ok(contactService.getContacts(pageable));
    }

    @GetMapping("/{contactId}")
    public ResponseEntity<ContactResponse> getContact(
            @PathVariable UUID contactId) {

        return ResponseEntity.ok(contactService.getContact(contactId));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> removeContact(
            @PathVariable UUID contactId) {

        contactService.removeContact(contactId);

        return ResponseEntity.noContent().build();
    }
}
