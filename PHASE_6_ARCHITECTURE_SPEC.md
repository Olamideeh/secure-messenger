# Secure Messenger — Phase 6 Architecture Specification

**Phase:** Encrypted Messaging  
**Roadmap:** Days 39–48  
**Status:** Architecture draft — pending final review  
**Implementation status:** Not started

## 1. Objective

Implement reliable, end-to-end encrypted direct messaging for Secure Messenger.

The first release must support:
- Sending and receiving encrypted text messages.
- Multiple registered devices per account.
- Message history and cursor pagination.
- Offline message retrieval.
- Per-device delivery and read receipts.
- Real-time notifications.
- Duplicate submission prevention.

The backend must never require access to plaintext messages or private cryptographic keys.

## 2. Technology

| Component | Technology |
|---|---|
| Frontend | React + TypeScript |
| Backend | Java + Spring Boot |
| Database | PostgreSQL |
| Database migrations | Flyway |
| Authentication | Spring Security session authentication |
| Real-time notifications | WebSocket |
| Encryption | Client-side E2EE protocol — pending validation |
| Future clients | Android and iOS |

## 3. Device architecture

A user can have multiple registered devices.

Each device has:
- UUID primary key.
- User ownership.
- Device name.
- Platform.
- Registration status.
- Creation timestamp.
- Approval timestamp.
- Last-seen timestamp.
- Revocation timestamp.

Device statuses:
- PENDING
- ACTIVE
- REVOKED

Private cryptographic keys are generated and retained on the client.

The backend stores device metadata and only the public cryptographic material required by the selected protocol.

Additional devices require approval from an existing trusted device. Phone OTP alone does not grant access to old encrypted history.

## 4. Device authentication

Existing session authentication identifies the user.

Messaging requires the backend to identify both:
- Authenticated user UUID.
- Authenticated device UUID.

The device identity must be established through a trusted server-side registration and authentication flow.

The backend must not trust a device ID supplied in a message request as proof of identity.

Device revocation must prevent future messaging operations and invalidate associated sessions.

Existing CSRF protection must remain enabled.

## 5. Message data model

### messages

One record represents one logical message.

Proposed fields:
- id — UUID primary key.
- conversation_id — foreign key.
- sender_user_id — foreign key.
- sender_device_id — foreign key.
- client_message_id — client-generated UUID.
- created_at — database-generated timestamp.

Constraints:
- Sender device must belong to sender user.
- Unique `(sender_device_id, client_message_id)`.
- Index on `(conversation_id, created_at, id)`.

### message_payloads

Stores opaque encrypted payload data.

Proposed fields:
- id — UUID primary key.
- message_id — foreign key.
- payload_scope — protocol-independent delivery scope.
- recipient_device_id — nullable foreign key where applicable.
- protocol_version.
- encrypted_payload — BYTEA.
- created_at — database-generated timestamp.

The exact payload structure and cardinality depend on the selected E2EE protocol.

Do not finalize protocol-specific fields before the technical proof of concept.

### message_receipts

Tracks delivery and reading per recipient device.

Proposed fields:
- id — UUID primary key.
- message_id — foreign key.
- recipient_device_id — foreign key.
- delivered_at — nullable timestamp.
- read_at — nullable timestamp.

Constraints:
- Unique `(message_id, recipient_device_id)`.
- Reading requires prior delivery.
- Receipt updates must be authorized for the authenticated device.
- Repeated acknowledgements must not overwrite the original timestamps.

## 6. Message submission flow

1. Client creates a unique `client_message_id`.
2. Client encrypts the message locally.
3. Client submits the encrypted payload to the backend.
4. Backend authenticates the user and device.
5. Backend verifies device status and conversation membership.
6. Backend validates recipient-device eligibility.
7. Backend checks idempotency.
8. Backend persists message and payload records transactionally.
9. Backend publishes notifications to connected devices.
10. Recipient devices retrieve and decrypt their authorized payloads locally.
11. Recipient devices acknowledge delivery and reading.

## 7. REST API contracts

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/devices/register` | Register device |
| GET | `/api/devices` | List devices |
| POST | `/api/devices/{id}/approve` | Approve device |
| DELETE | `/api/devices/{id}` | Revoke device |
| POST | `/api/conversations/{conversationId}/messages` | Submit encrypted message |
| GET | `/api/conversations/{conversationId}/messages` | Retrieve message history |
| GET | `/api/messages/pending` | Retrieve pending device deliveries |
| POST | `/api/messages/{messageId}/receipts/delivered` | Confirm delivery |
| POST | `/api/messages/{messageId}/receipts/read` | Confirm reading |

All endpoints must use the authenticated server-side user and device identity.

## 8. Pagination

Use cursor-based pagination.

Stable ordering:

`created_at DESC, id DESC`

Proposed defaults:
- Page size: 30.
- Maximum page size: 100.

The cursor must be opaque to the frontend and must preserve deterministic ordering.

## 9. WebSocket architecture

Proposed endpoint:

`/ws/messages`

WebSocket provides real-time message-available notifications.

PostgreSQL remains the durable source of truth.

A successful WebSocket publication does not count as delivery confirmation.

Offline devices retrieve pending messages through REST after reconnecting.

Connection and subscription authorization must be tied to the authenticated device.

## 10. Security requirements

- No plaintext message storage.
- No private-key uploads.
- Active device required for sending.
- Active conversation membership required.
- Recipient eligibility checked by the backend.
- Revoked devices cannot send, retrieve new deliveries, or acknowledge receipts.
- Users cannot access another user's device records.
- Devices cannot acknowledge another device's receipts.
- Duplicate submissions cannot create duplicate messages.
- Do not log plaintext, private keys, or sensitive encrypted payloads.
- Enforce payload size limits and rate limits.
- Protect browser key material against XSS and unsafe client-side storage.
- Preserve CSRF protection for session-authenticated requests.

## 11. Backend package structure

`com.secure.messenger`

- `device`
    - controller
    - service
    - repository
    - entity
    - dto
    - enums
- `message`
    - controller
    - service
    - repository
    - entity
    - dto
    - enums
- `websocket`
    - config
    - event
    - service
- `security`
    - existing authentication
    - device-bound authentication
    - authorization

Existing conversation functionality remains in its current module.

## 12. Migration plan

Proposed migration responsibilities:

| Migration | Responsibility |
|---|---|
| Next available version | Devices table |
| Following version | Protocol-specific public-key material |
| Following version | Messages table |
| Following version | Message payloads table |
| Following version | Message receipts table |
| Following version | Additional indexes and integrity constraints |

Actual version numbers must be checked against Flyway history before implementation.

Never edit an already-applied migration.

Never delete the database or use Flyway repair to bypass checksum problems.

## 13. Implementation sequence

| Roadmap day | Work |
|---|---|
| 39 | Architecture and data model |
| 40 | Device entity and migration |
| 41 | Device repository, service and API |
| 42 | Device-bound authentication |
| 43 | Message entities and migrations |
| 44 | Message repositories and DTOs |
| 45 | Message submission and retrieval |
| 46 | Delivery/read receipts and idempotency |
| 47 | WebSocket and offline recovery |
| 48 | Integration and security testing |

Protocol validation is a dependency before implementing actual encryption.

## 14. Encryption protocol decision

Two candidates remain under evaluation:

- Signal Protocol / libsignal.
- MLS / OpenMLS.

No protocol has been approved.

Before selection, validate:
- Browser compatibility.
- TypeScript integration.
- Android integration.
- iOS integration.
- Multi-device key management.
- Device revocation.
- Key rotation.
- Recovery behavior.
- Library maintenance and external-use support.
- Security review and production suitability.

No custom cryptographic protocol will be developed.

## 15. Definition of done

Phase 6 is complete when:

1. Users can register and manage trusted devices.
2. Sessions are bound to authenticated devices.
3. Clients can exchange encrypted direct messages.
4. Backend cannot decrypt message content.
5. Duplicate submissions are safely handled.
6. Message history supports cursor pagination.
7. Delivery and read receipts work per device.
8. Offline devices can retrieve pending messages.
9. WebSocket notifications work with authorization.
10. Integration and security tests pass.
11. Changes are committed and pushed to GitHub.
12. A LinkedIn milestone post is prepared.

**Important:** This specification is an engineering design, not a cryptographic audit or proof of security. Protocol-specific decisions remain open until validated.