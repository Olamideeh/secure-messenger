
package com.secure.messenger.service;

import com.secure.messenger.dto.ContactRequestResponse;
import com.secure.messenger.dto.SendContactRequest;
import com.secure.messenger.entity.ContactRequest;
import com.secure.messenger.entity.User;
import com.secure.messenger.enums.ContactRequestStatus;
import com.secure.messenger.repository.ContactRepository;
import com.secure.messenger.repository.ContactRequestRepository;
import com.secure.messenger.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ContactRequestService {

    private final ContactRequestRepository contactRequestRepository;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    public ContactRequestService(
            ContactRequestRepository contactRequestRepository,
            UserRepository userRepository,
            ContactRepository contactRepository
    ) {
        this.contactRequestRepository = contactRequestRepository;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    @Transactional
    public ContactRequestResponse createContactRequest(
            SendContactRequest request
    ) {

        // 1. Get the authenticated user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated");
        }

        // 2. Resolve the authenticated user's ID
        String authenticatedName = authentication.getName();

        Optional<User> senderResult;

        try {
            UUID senderId = UUID.fromString(authenticatedName);
            senderResult = userRepository.findById(senderId);
        } catch (IllegalArgumentException exception) {
            // If the authentication name is the phone number
            senderResult = userRepository.findByPhoneNumber(authenticatedName);
        }

        User sender = senderResult.orElseThrow(
                () -> new IllegalArgumentException("Sender account not found")
        );

        // 3. Find the recipient by phone number
        User recipient = userRepository.findByPhoneNumber(
                request.getRecipientPhoneNumber()
        ).orElseThrow(
                () -> new IllegalArgumentException("Recipient not found")
        );

        // 4. Prevent sending a request to yourself
        if (sender.getId().equals(recipient.getId())) {
            throw new IllegalArgumentException(
                    "You cannot send a contact request to yourself"
            );
        }

        // 5. Check whether they are already contacts
        boolean alreadyContacts =
                contactRepository.existsContactBetweenUsers(
                        sender.getId(),
                        recipient.getId()
                );

        if (alreadyContacts) {
            throw new IllegalStateException(
                    "You are already contacts"
            );
        }

        // 6. Check whether a pending request already exists
        boolean pendingRequestExists =
                contactRequestRepository.existsPendingBetweenUsers(
                        sender.getId(),
                        recipient.getId(),
                        ContactRequestStatus.PENDING
                );

        if (pendingRequestExists) {
            throw new IllegalStateException(
                    "A pending contact request already exists"
            );
        }

        // 7. Create a new contact request
        ContactRequest contactRequest = new ContactRequest();

        contactRequest.setSender(sender);
        contactRequest.setRecipient(recipient);
        contactRequest.setStatus(ContactRequestStatus.PENDING);

        // 8. Save the request
        ContactRequest savedRequest =
                contactRequestRepository.save(contactRequest);

        // 9. Convert the entity into a response DTO
        return new ContactRequestResponse(
                savedRequest.getId(),
                savedRequest.getSender().getId(),
                savedRequest.getRecipient().getId(),
                savedRequest.getStatus(),
                savedRequest.getCreatedAt(),
                savedRequest.getRespondedAt()
        );
    }
}
