# Secure Messenger — Security Threat Model

**Document:** SECURITY_THREAT_MODEL.md  
**Project:** Secure Messenger  
**Phase:** Device Identity & End-to-End Encryption Design  
**Status:** Initial design — subject to review and updates

## 1. Purpose

This document identifies the important assets, potential attackers, security threats, and protections required to build Secure Messenger.

The main security objective is to ensure that only the intended users and their authorized devices can read private messages.

Secure Messenger will use end-to-end encryption (E2EE). Message encryption and decryption must happen on user devices, not on the backend server.

We will use an established and maintained cryptographic protocol rather than creating our own encryption system.

## 2. Assets to Protect

The following assets require protection:

| Asset | Why it must be protected |
|---|---|
| User accounts | Prevent unauthorized access and impersonation |
| Phone numbers | Protect personal information |
| Private encryption keys | Prevent attackers and the server from decrypting messages |
| Public identity keys | Help establish and verify device identities |
| Message contents | Keep private conversations confidential |
| Encrypted attachments | Prevent unauthorized access to shared media |
| Authentication tokens and sessions | Prevent account takeover |
| Device records | Identify and manage authorized devices |
| Contact and conversation metadata | Reduce unnecessary exposure of user relationships |

Private encryption keys must remain under the control of user devices. They must never be stored as ordinary plaintext database records on the backend.

## 3. Potential Attackers

### 3.1 Unauthorized users
People attempting to access another user's account without permission.

### 3.2 Attackers with a stolen device
Someone who obtains a user's phone or computer and attempts to access its stored data or active session.

### 3.3 Malicious server administrators
An administrator or someone with excessive server privileges who attempts to inspect private user messages or sensitive information.

### 3.4 Network attackers
People attempting to intercept, modify, or replay data exchanged between the application and backend.

### 3.5 Device impersonators
Attackers attempting to register a fake device or pretend to be an already trusted device.

### 3.6 Malicious insiders or compromised services
Someone with access to application infrastructure, logs, backups, or integrations who attempts to misuse that access.

## 4. Security Threats and Mitigations

| Threat | Possible impact | Proposed mitigation |
|---|---|---|
| Account takeover | Attacker gains access to a user's account | Strong authentication, secure sessions, rate limiting, and account recovery protections |
| Private key theft | Attacker may decrypt messages or impersonate a device | Generate keys on devices and protect private keys using platform secure storage |
| Man-in-the-middle attack | Messages or authentication traffic may be intercepted or modified | HTTPS/TLS, authenticated key exchange, and identity verification |
| Device impersonation | Attacker pretends to be a trusted device | Device identity keys, authenticated device registration, and key-change notifications |
| Message replay or modification | Old messages may be resent or messages altered | Use protocol-provided message authentication, replay protection, and unique message identifiers |
| Stolen device | Unauthorized person may access an active account or device keys | Device locking, secure local storage, session revocation, and device management |
| Server compromise | Attacker obtains stored messages and account metadata | Store ciphertext rather than plaintext message bodies, restrict access, and protect sensitive metadata |
| OTP abuse | Attackers attempt repeated verification or account enumeration | OTP expiry, attempt limits, resend cooldowns, rate limiting, and generic responses |

These mitigations are design requirements. They must be implemented and tested before production launch.

## 5. Security Objectives

Secure Messenger must aim to provide the following protections.

### 5.1 Message confidentiality
Only intended recipient devices should be able to decrypt message contents.

### 5.2 Message integrity
Recipients should be able to detect unauthorized modification of messages.

### 5.3 Message authenticity
The encryption protocol should provide a way to establish that messages come from an authenticated sender device.

### 5.4 Device identity verification
Every registered device must have its own cryptographic identity. Users should have a way to detect unexpected identity-key changes.

### 5.5 Private key protection
Private keys must be generated and protected on user devices. They must not be exposed through API responses, ordinary application logs, or backend database records.

### 5.6 Device revocation
Users must be able to revoke a lost or compromised device so it can no longer access new account activity.

### 5.7 Secure communication
Communication between clients and the backend must use HTTPS in production and WebSocket over TLS for real-time messaging.

## 6. Server Responsibilities and Limitations

The backend is responsible for:

- User registration and authentication.
- Device registration and management.
- Storing public device identity information.
- Managing contacts and conversations.
- Receiving, routing, and storing encrypted message envelopes.
- Managing message delivery states.
- Enforcing authorization and abuse-prevention rules.

The backend must not:

- Store users' private encryption keys as ordinary server-side records.
- Receive plaintext message bodies for normal E2EE messaging.
- Decrypt messages on behalf of users.
- Log plaintext messages, authentication tokens, or private keys.

The server may still see certain metadata, such as sender and recipient identifiers, delivery times, device identifiers, and message routing information. E2EE does not automatically hide all metadata.

## 7. Device and Account Recovery Trade-offs

### 7.1 Lost phone
A user should be able to revoke the lost device and prevent it from receiving future messages.

### 7.2 New phone
A new device must establish its own identity and keys. The application must decide how the user securely authorizes it and whether old encrypted history can be recovered.

### 7.3 All devices lost
If a user loses every registered device, recovering the account must not automatically give the server access to old private messages.

A recovery mechanism must balance account recovery with encryption security. This decision requires careful design.

### 7.4 Stolen device
The user should be able to revoke a stolen device through another authorized device or a carefully designed account recovery process.

Revocation must prevent future access, but it cannot guarantee deletion of messages or keys already copied by an attacker.

## 8. Cryptographic Design Principles

Secure Messenger will follow these principles:

1. Use an established, maintained E2EE protocol and suitable implementation.
2. Never invent a custom encryption algorithm or protocol.
3. Generate and protect private keys on user devices.
4. Treat public keys as public information, but authenticate their association with devices.
5. Design for multiple devices per account.
6. Consider key rotation, revocation, and recovery before implementation.
7. Evaluate web and future Android/iOS compatibility.
8. Test encryption, decryption, identity verification, and failure scenarios.

## 9. Known Limitations and Open Decisions

The following decisions are not yet finalized:

- Which E2EE protocol and implementation will be selected.
- How browser-based private keys will be stored and protected.
- How users will verify device identities.
- How new devices will be authorized.
- Whether and how encrypted message history can be recovered.
- How key changes and device revocation will affect existing conversations.
- How multi-device message delivery will work.

These decisions will be addressed during the Device Identity & E2EE Design phase.

## 10. Security Readiness Criteria

Before claiming that Secure Messenger provides production-ready E2EE:

- A maintained protocol implementation must be selected.
- A documented threat model must be reviewed.
- Private-key storage and lifecycle must be implemented.
- Two-device encryption and decryption must be demonstrated.
- The backend must be shown to handle ciphertext without accessing message plaintext.
- Device revocation and identity-change behavior must be tested.
- Security testing and independent review should be considered before public launch.

**Conclusion:** Security is a core requirement of Secure Messenger, not an optional feature. Every future messaging feature must respect this threat model and the principle that private message contents remain under the control of authorized user devices.