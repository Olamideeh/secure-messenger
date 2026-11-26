
package com.secure.messenger.service;

import com.secure.messenger.dto.ContactResponse;
import com.secure.messenger.entity.Contact;
import com.secure.messenger.entity.User;
import com.secure.messenger.repository.ContactRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    // GET ALL CONTACTS
    @Transactional(readOnly = true)
    public Page<ContactResponse> getContacts(Pageable pageable) {

        UUID userId = getAuthenticatedUserId();

        return contactRepository
                .findAllContactsByUserId(userId, pageable)
                .map(contact -> toResponse(contact, userId));
    }

    // GET ONE CONTACT
    @Transactional(readOnly = true)
    public ContactResponse getContact(UUID contactId) {

        UUID userId = getAuthenticatedUserId();

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Contact not found"));

        if (!contact.getUserLow().getId().equals(userId)
                && !contact.getUserHigh().getId().equals(userId)) {
            throw new IllegalStateException(
                    "You are not authorized to view this contact");
        }

        return toResponse(contact, userId);
    }

    // REMOVE CONTACT
    @Transactional
    public void removeContact(UUID contactId) {

        UUID userId = getAuthenticatedUserId();

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Contact not found"));

        if (!contact.getUserLow().getId().equals(userId)
                && !contact.getUserHigh().getId().equals(userId)) {
            throw new IllegalStateException(
                    "You are not authorized to remove this contact");
        }

        // Only remove the contact relationship.
        // Conversations and message history remain untouched.
        contactRepository.delete(contact);
    }

    private ContactResponse toResponse(Contact contact, UUID authenticatedUserId) {

        User otherUser = contact.getUserLow().getId()
                .equals(authenticatedUserId)
                ? contact.getUserHigh()
                : contact.getUserLow();

        return new ContactResponse(
                contact.getId(),
                otherUser.getId(),
                otherUser.getPhoneNumber(),
                contact.getCreatedAt()
        );
    }

    private UUID getAuthenticatedUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated");
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid authenticated user identity");
        }
    }
}
