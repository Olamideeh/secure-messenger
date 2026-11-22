# KEY_LIFECYCLE_DESIGN.md

## 1. Purpose

This document defines how Secure Messenger will create, store, use, rotate, revoke, and recover cryptographic keys.

The objective is to ensure that private encryption keys remain under the control of users' devices and that the backend cannot read users' encrypted messages.

This design is intended for the first web beta, while keeping future Android and iOS clients in mind.

## 2. Core Security Principles

1. Private keys must never be sent to or stored by the backend.
2. Cryptographic operations must happen on the user's device.
3. We must use a maintained, independently reviewed cryptographic protocol or library.
4. The backend may store public keys and encrypted message data.
5. Every device must have its own cryptographic identity.
6. Users must be able to revoke a lost or compromised device.
7. Key changes must be detectable by other devices and contacts.
8. Key recovery must not silently weaken end-to-end encryption.

## 3. Types of Keys

The following key categories are part of the planned design. The selected protocol implementation will determine the exact key formats and algorithms.

### A. Identity key pair

Each device has a long-term identity key pair.

- Public identity key: shared with the backend and other users.
- Private identity key: remains on the device.

Purpose:
- Establish device identity.
- Authenticate cryptographic relationships.
- Help contacts detect unexpected identity changes.

### B. Signed prekey

A signed prekey is a public key signed using the device's identity key.

- The public signed prekey can be uploaded to the backend.
- Its corresponding private key stays on the device.
- It must be periodically rotated.

Purpose:
- Allow another device to initiate a secure session even when the recipient is offline.

### C. One-time prekeys

One-time prekeys are public keys uploaded in batches.

- Each has a corresponding private key stored locally.
- The backend distributes public one-time prekeys to other devices.
- A one-time prekey should not be reused after successful consumption.

Purpose:
- Support asynchronous session establishment.
- Improve forward secrecy during initial session setup.

### D. Session keys

Session keys are established between communicating devices through the selected protocol.

- They are generated and managed by the cryptographic implementation.
- They must not be stored as plaintext on the backend.
- They evolve as messages are exchanged.

Purpose:
- Encrypt and decrypt message content.

## 4. Key Generation

When a user registers a new device:

1. The client initializes the approved cryptographic implementation.
2. The device generates its identity key pair.
3. The device generates its signed prekey.
4. The device generates a batch of one-time prekeys.
5. Private key material is stored locally using an appropriate secure storage mechanism.
6. Public key material is submitted to the backend.
7. The backend associates the public keys with the authenticated account and registered device.

The backend must validate ownership and authorization before accepting key uploads.

## 5. Private Key Storage

For the web client:

- Use browser-supported secure cryptographic facilities where compatible with the selected protocol.
- Store private key material in an appropriately protected local storage mechanism.
- Do not store private keys in localStorage.
- Do not expose private keys through application logs, analytics, API responses, or debugging output.
- Protect the application against cross-site scripting (XSS), because malicious scripts executing in the application origin could potentially access cryptographic operations or key material.

For future mobile clients:

- Android should use Android Keystore where compatible.
- iOS should use Keychain and appropriate platform security facilities.

The exact storage implementation must be validated against the selected protocol library before development.

## 6. Key Rotation

Keys should not remain valid indefinitely.

The system will support:

- Periodic signed prekey rotation.
- Replacement of consumed one-time prekeys.
- Session-key evolution as defined by the selected protocol.
- Identity-key replacement when a device is re-registered or its identity is compromised.

A key rotation must not silently make an existing contact trust a new identity.

Contacts should be informed when a device identity changes and should have a way to verify the new identity.

## 7. Key Revocation

A user must be able to revoke a device that is lost, stolen, or no longer trusted.

When a device is revoked:

1. The backend marks the device as revoked.
2. The backend prevents it from authenticating or uploading new messages.
3. Its public keys are no longer offered for new session establishment.
4. Other devices and contacts are notified of the device or identity change.
5. New secure sessions are established where required.

Important limitation:

Revocation cannot erase private keys or messages that an attacker has already copied from a compromised device.

## 8. Multi-device Support

The first beta should establish a device-aware design even if advanced multi-device synchronization is not included immediately.

Each registered device must have:

- A unique device ID.
- Its own identity key pair.
- Its own signed prekey.
- Its own one-time prekeys.
- A registration and revocation status.

Messages must be encrypted for the intended recipient devices using the selected protocol's supported multi-device approach.

The backend must not decrypt messages to synchronize them between devices.

## 9. Key Recovery

Key recovery is a security-sensitive feature.

The system must distinguish between:

- Recovering an account.
- Registering a replacement device.
- Recovering access to old encrypted message history.

Verifying a phone number may prove control of an account's phone number, but it does not automatically recover the old device's private encryption keys.

The first beta will not promise recovery of old encrypted message history when all trusted devices and recovery material are lost.

Any future recovery mechanism must be designed so that the backend cannot secretly obtain users' private keys.

## 10. Backend Responsibilities

The backend may:

- Store public identity keys.
- Store signed public prekeys.
- Store available public one-time prekeys.
- Associate keys with registered devices.
- Track key publication and rotation metadata.
- Mark devices as revoked.
- Distribute public key bundles to authorized clients.

The backend must not:

- Store private identity keys.
- Store private prekey material.
- Decrypt message content.
- Generate users' private encryption keys.
- Log plaintext messages or private key material.

## 11. Important Risks

| Risk | Required protection |
|---|---|
| Private key theft | Secure local storage and device protection |
| Malicious JavaScript | XSS prevention and strict content security |
| Public key substitution | Authenticated key association and identity verification |
| Reuse of one-time prekeys | Server-side consumption tracking |
| Lost device | Device revocation |
| Unexpected identity change | Contact notification and verification |
| Lost private keys | Honest recovery limitations |

## 12. Decisions Still Requiring Validation

Before implementation, we must confirm:

- The selected protocol supports the required browser environment.
- The library's license and usage restrictions permit our intended product.
- The implementation supports future Android and iOS clients.
- Its multi-device design meets our requirements.
- Its key storage requirements can be met in browsers.
- Its recovery and identity verification mechanisms are suitable.

No custom cryptographic algorithms will be implemented.

## 13. Acceptance Criteria

This design is considered complete when:

- Key categories and their purposes are documented.
- Private and public key responsibilities are separated.
- Device-specific key ownership is defined.
- Key rotation and revocation requirements are documented.
- Multi-device requirements are identified.
- Recovery limitations are documented.
- Backend restrictions are explicit.
- The design remains subject to validation against the final selected protocol.

**Status:** Design documented. Protocol-specific implementation details remain subject to library selection and security review.