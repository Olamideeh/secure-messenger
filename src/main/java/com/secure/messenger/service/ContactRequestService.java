
package com.secure.messenger.service;

import com.secure.messenger.dto.ContactRequestResponse;
import com.secure.messenger.dto.SendContactRequest;
import com.secure.messenger.entity.Contact;
import com.secure.messenger.entity.ContactRequest;
import com.secure.messenger.entity.User;
import com.secure.messenger.enums.ContactRequestStatus;
import com.secure.messenger.repository.ContactRepository;
import com.secure.messenger.repository.ContactRequestRepository;
import com.secure.messenger.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class ContactRequestService {

    private final ContactRequestRepository contactRequestRepository;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    public ContactRequestService(
            ContactRequestRepository contactRequestRepository,
            UserRepository userRepository,
            ContactRepository contactRepository) {
        this.contactRequestRepository = contactRequestRepository;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    // CREATE CONTACT REQUEST
    @Transactional
    public ContactRequestResponse createContactRequest(SendContactRequest request) {

        User sender = getAuthenticatedUser();

        User recipient = userRepository.findByPhoneNumber(
                request.getRecipientPhoneNumber()
        ).orElseThrow(() ->
                new IllegalArgumentException("Recipient not found"));

        if (sender.getId().equals(recipient.getId())) {
            throw new IllegalArgumentException(
                    "You cannot send a contact request to yourself");
        }

        if (contactRepository.existsContactBetweenUsers(
                sender.getId(), recipient.getId())) {
            throw new IllegalStateException("You are already contacts");
        }

        if (contactRequestRepository.existsPendingBetweenUsers(
                sender.getId(),
                recipient.getId(),
                ContactRequestStatus.PENDING)) {
            throw new IllegalStateException(
                    "A pending contact request already exists");
        }

        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setSender(sender);
        contactRequest.setRecipient(recipient);
        contactRequest.setStatus(ContactRequestStatus.PENDING);

        ContactRequest savedRequest =
                contactRequestRepository.save(contactRequest);

        return toResponse(savedRequest);
    }

    // GET INCOMING REQUESTS
    @Transactional(readOnly = true)
    public Page<ContactRequestResponse> getIncomingRequests(Pageable pageable) {

        UUID userId = getAuthenticatedUserId();

        return contactRequestRepository
                .findByRecipient_Id(userId, pageable)
                .map(this::toResponse);
    }

    // GET OUTGOING REQUESTS
    @Transactional(readOnly = true)
    public Page<ContactRequestResponse> getOutgoingRequests(Pageable pageable) {

        UUID userId = getAuthenticatedUserId();

        return contactRequestRepository
                .findBySender_Id(userId, pageable)
                .map(this::toResponse);
    }

    // ACCEPT CONTACT REQUEST
    @Transactional
    public ContactRequestResponse acceptContactRequest(UUID requestId) {

        UUID authenticatedUserId = getAuthenticatedUserId();

        ContactRequest request = contactRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Contact request not found"));

        if (!request.getRecipient().getId().equals(authenticatedUserId)) {
            throw new IllegalStateException(
                    "You are not authorized to accept this request");
        }

        if (request.getStatus() != ContactRequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending requests can be accepted");
        }

        User sender = request.getSender();
        User recipient = request.getRecipient();

        if (contactRepository.existsContactBetweenUsers(
                sender.getId(), recipient.getId())) {
            throw new IllegalStateException(
                    "A contact relationship already exists");
        }

        Contact contact = new Contact();

        if (sender.getId().compareTo(recipient.getId()) < 0) {
            contact.setUserLow(sender);
            contact.setUserHigh(recipient);
        } else {
            contact.setUserLow(recipient);
            contact.setUserHigh(sender);
        }

        contactRepository.save(contact);

        request.setStatus(ContactRequestStatus.ACCEPTED);
        request.setRespondedAt(OffsetDateTime.now(ZoneOffset.UTC));

        ContactRequest savedRequest =
                contactRequestRepository.save(request);

        return toResponse(savedRequest);
    }

    // REJECT CONTACT REQUEST
    @Transactional
    public ContactRequestResponse rejectContactRequest(UUID requestId) {

        UUID authenticatedUserId = getAuthenticatedUserId();

        ContactRequest request = contactRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Contact request not found"));

        if (!request.getRecipient().getId().equals(authenticatedUserId)) {
            throw new IllegalStateException(
                    "You are not authorized to reject this request");
        }

        if (request.getStatus() != ContactRequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending requests can be rejected");
        }

        request.setStatus(ContactRequestStatus.REJECTED);
        request.setRespondedAt(OffsetDateTime.now(ZoneOffset.UTC));

        ContactRequest savedRequest =
                contactRequestRepository.save(request);

        return toResponse(savedRequest);
    }

    // GET AUTHENTICATED USER
    private User getAuthenticatedUser() {

        UUID userId = getAuthenticatedUserId();

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"));
    }

    // GET AUTHENTICATED USER ID
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

    // CONVERT ENTITY TO RESPONSE DTO
    private ContactRequestResponse toResponse(ContactRequest request) {

        return new ContactRequestResponse(
                request.getId(),
                request.getSender().getId(),
                request.getRecipient().getId(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getRespondedAt()
        );
    }
}
