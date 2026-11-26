
package com.secure.messenger.service;

import com.secure.messenger.dto.ConversationResponse;
import com.secure.messenger.entity.Contact;
import com.secure.messenger.entity.Conversation;
import com.secure.messenger.entity.ConversationMember;
import com.secure.messenger.entity.User;
import com.secure.messenger.enums.ConversationMemberRole;
import com.secure.messenger.enums.ConversationType;
import com.secure.messenger.repository.ContactRepository;
import com.secure.messenger.repository.ConversationMemberRepository;
import com.secure.messenger.repository.ConversationRepository;
import com.secure.messenger.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            ConversationMemberRepository memberRepository,
            ContactRepository contactRepository,
            UserRepository userRepository) {

        this.conversationRepository = conversationRepository;
        this.memberRepository = memberRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    // CREATE OR GET DIRECT CONVERSATION
    @Transactional
    public ConversationResponse createDirectConversation(UUID recipientId) {

        UUID authenticatedUserId = getAuthenticatedUserId();

        if (authenticatedUserId.equals(recipientId)) {
            throw new IllegalArgumentException(
                    "You cannot create a conversation with yourself");
        }

        User authenticatedUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() ->
                        new IllegalStateException("Authenticated user not found"));

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Recipient not found"));

        Contact contact = contactRepository.findContactBetweenUsers(
                authenticatedUserId, recipientId
        ).orElseThrow(() ->
                new IllegalStateException(
                        "You must be contacts before starting a conversation"));

        UUID lowId = authenticatedUserId.compareTo(recipientId) < 0
                ? authenticatedUserId : recipientId;

        UUID highId = authenticatedUserId.compareTo(recipientId) < 0
                ? recipientId : authenticatedUserId;

        // Return an existing direct conversation if one already exists.
        var existing = conversationRepository.findDirectConversationBetweenUsers(
                lowId,
                highId,
                ConversationType.DIRECT
        );

        if (existing.isPresent()) {
            Conversation conversation = existing.get();

            ensureActiveMembership(conversation, authenticatedUser);

            return toResponse(conversation);
        }

        User lowUser = authenticatedUserId.equals(lowId)
                ? authenticatedUser : recipient;

        User highUser = authenticatedUserId.equals(highId)
                ? authenticatedUser : recipient;

        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.DIRECT);
        conversation.setDirectUserLow(lowUser);
        conversation.setDirectUserHigh(highUser);

        Conversation savedConversation =
                conversationRepository.save(conversation);

        createMembership(savedConversation, authenticatedUser);
        createMembership(savedConversation, recipient);

        return toResponse(savedConversation);
    }

    // GET ALL ACTIVE CONVERSATIONS
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getConversations(Pageable pageable) {

        UUID authenticatedUserId = getAuthenticatedUserId();

        return conversationRepository
                .findUserConversations(authenticatedUserId, pageable)
                .map(this::toResponse);
    }

    // GET ONE CONVERSATION
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID conversationId) {

        UUID authenticatedUserId = getAuthenticatedUserId();

        requireActiveMembership(conversationId, authenticatedUserId);

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Conversation not found"));

        return toResponse(conversation);
    }

    // CREATE MEMBERSHIP
    private void createMembership(
            Conversation conversation,
            User user) {

        ConversationMember member = new ConversationMember();

        member.setConversation(conversation);
        member.setUser(user);
        member.setRole(ConversationMemberRole.MEMBER);

        memberRepository.save(member);
    }

    // ENSURE AUTHENTICATED USER HAS ACTIVE MEMBERSHIP
    private void ensureActiveMembership(
            Conversation conversation,
            User user) {

        ConversationMember member = memberRepository
                .findByConversation_IdAndUser_Id(
                        conversation.getId(),
                        user.getId()
                )
                .orElse(null);

        if (member == null) {
            createMembership(conversation, user);
        } else if (member.getLeftAt() != null) {
            member.setLeftAt(null);
            memberRepository.save(member);
        }
    }

    // VERIFY ACTIVE MEMBERSHIP
    private void requireActiveMembership(
            UUID conversationId,
            UUID userId) {

        if (!memberRepository
                .existsByConversation_IdAndUser_IdAndLeftAtIsNull(
                        conversationId,
                        userId)) {
            throw new IllegalStateException(
                    "You are not an active member of this conversation");
        }
    }

    // CONVERT ENTITY TO RESPONSE DTO
    private ConversationResponse toResponse(Conversation conversation) {

        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
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
}
