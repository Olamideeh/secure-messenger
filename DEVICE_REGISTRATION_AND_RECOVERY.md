# DEVICE_REGISTRATION_AND_RECOVERY.md

## 1. Purpose

This document defines how devices register with Secure Messenger, how additional devices become trusted, and how users recover access when a device is lost or compromised.

The goal is to support multiple devices without weakening end-to-end encryption.

A verified phone number establishes account access, but it does not automatically establish trust in a device's encryption keys.

## 2. Core Design Principles

1. Every device must have a unique device identity.
2. Every device must generate and control its own private cryptographic keys.
3. The backend must never receive private encryption keys.
4. A phone number alone must not silently authorize access to existing encrypted message history.
5. Adding a new device must require an explicit authorization process.
6. Users must be able to view and revoke registered devices.
7. Device revocation must prevent future access through the revoked device.
8. Recovery must not create a hidden backdoor into encrypted messages.

## 3. Device Registration

Each account can have one or more registered devices.

A device record should contain:

| Field | Purpose |
|---|---|
| `id` | Unique device identifier |
| `user_id` | Account that owns the device |
| `device_name` | User-friendly device name |
| `platform` | Web, Android, or iOS |
| `identity_public_key` | Public identity key |
| `signed_prekey` | Current signed public prekey |
| `registration_status` | Pending, active, or revoked |
| `created_at` | Registration timestamp |
| `last_seen_at` | Last successful activity |
| `revoked_at` | Revocation timestamp, if applicable |

Private keys must never be included in this record.

The backend stores public key material and device metadata only.

## 4. First Device Registration

When a user creates an account and verifies their phone number:

1. The user opens Secure Messenger on their browser.
2. The backend confirms that the account's phone number is verified.
3. The browser initializes the approved cryptographic implementation.
4. The browser generates its device identity and required key material.
5. Private key material is stored locally.
6. The browser submits its public key bundle and device metadata.
7. The backend validates the request and associates the device with the account.
8. The device becomes active.
9. The user can begin establishing secure conversations.

The server must verify that the request belongs to the authenticated account.

The first registered device becomes the initial trusted device for that account.

## 5. Adding Another Device

A user may want to access their account from another browser, computer, or mobile phone.

The new device must not automatically inherit trust simply because the user enters the correct phone number and OTP.

### Proposed authorization flow

1. The user opens Secure Messenger on the new device.
2. The new device authenticates the account using the approved account authentication process.
3. The new device generates its own identity key pair.
4. The new device enters a pending registration state.
5. The existing trusted device receives a notification that a new device wants to join.
6. The user reviews the request and explicitly approves or rejects it.
7. The existing device securely authorizes the new device using the selected cryptographic protocol's device-linking mechanism.
8. The backend activates the new device after validating the authorization.
9. Other devices are informed of the new device registration.

If no trusted device is available, the user must follow the account recovery process.

The backend must not pretend that phone verification alone provides cryptographic authorization from an existing device.

## 6. Device Verification

Users should be able to inspect registered devices.

For every device, display:

- Device name.
- Platform.
- Registration date.
- Last active time.
- Current status.

Users should also have a way to verify device identities.

The final verification mechanism will depend on the selected protocol. It may include comparing safety numbers, scanning a QR code, or another authenticated fingerprint verification method.

A device identity change should be visible to relevant contacts.

## 7. Lost Device Scenario

If a user loses a device but still has another trusted device:

1. The user signs in using an existing trusted device.
2. The user opens device management.
3. The lost device is identified.
4. The user revokes the lost device.
5. The backend marks it as revoked.
6. The backend rejects future authenticated requests from that device.
7. Its public keys are removed from new session establishment.
8. Other devices and affected contacts receive appropriate security notifications.

Revocation cannot erase private keys or plaintext messages already copied from the lost device.

## 8. All Devices Lost Scenario

If a user loses every trusted device:

1. The user verifies control of their phone number.
2. The backend confirms account ownership according to the account recovery policy.
3. The user registers a replacement device.
4. The replacement device generates new cryptographic keys.
5. Previous devices are marked revoked or otherwise invalidated according to the recovery policy.
6. Contacts are informed that the user's device identity has changed.
7. New secure sessions are established.

### Important limitation

Phone-number verification does not recover old private keys.

If the user has no remaining trusted device and no supported recovery material, old encrypted message history may be permanently inaccessible.

This is an intentional security trade-off.

We must never claim that old messages can be recovered unless a tested, secure recovery mechanism actually exists.

## 9. Device Revocation

The backend must support device revocation.

A revoked device must:

- Be unable to authenticate successfully.
- Be unable to register new public keys.
- Be excluded from future message delivery.
- Be excluded from new key-bundle distribution.
- Be marked as revoked in device management.

Revocation should be enforced on the backend, not merely hidden in the frontend.

Existing sessions and credentials associated with the device must also be invalidated.

## 10. Device Management API Requirements

The following API operations are planned.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/devices/register` | Register a device |
| GET | `/api/devices` | List account devices |
| GET | `/api/devices/{id}` | View device details |
| POST | `/api/devices/{id}/approve` | Approve a pending device |
| DELETE | `/api/devices/{id}` | Revoke a device |

These are proposed API contracts, not implemented endpoints.

All endpoints must require authentication and enforce account ownership.

A user must never be able to approve, inspect, or revoke another user's device.

The approval endpoint must validate cryptographic authorization rather than trusting a request parameter alone.

## 11. Backend and Frontend Responsibilities

### Backend responsibilities

- Authenticate account requests.
- Create and manage device records.
- Store public key material.
- Track pending, active, and revoked devices.
- Validate device registration and authorization.
- Enforce device revocation.
- Distribute public key bundles to authorized clients.
- Notify relevant devices about security events.

### Frontend responsibilities

- Generate cryptographic key material using the approved protocol.
- Protect private keys locally.
- Display device registration requests.
- Allow users to approve or reject new devices.
- Display registered devices.
- Allow users to revoke devices.
- Warn users about identity changes.
- Never send private key material to the backend.

## 12. Security Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Attacker registers a device using a stolen OTP | Require trusted-device authorization or a carefully designed recovery process |
| Unauthorized device approval | Cryptographic authorization and authenticated requests |
| Stolen device remains active | Device revocation and session invalidation |
| Backend modifies public keys | Authenticated key associations and identity verification |
| Account recovery exposes old messages | Do not equate phone verification with private-key recovery |
| User does not notice a new device | Security notifications and device management |
| Revoked device continues sending requests | Backend authorization checks on every relevant operation |

## 13. Recovery Policy for the First Beta

The first beta will use the following policy:

- Users can register their first device after account verification.
- Additional devices require explicit approval from an existing trusted device.
- Users can inspect and revoke registered devices.
- Losing one device does not affect access from other trusted devices.
- Losing all trusted devices allows account re-registration after verification, subject to the recovery policy.
- Old encrypted message history is not guaranteed to be recoverable.
- A replacement device receives a new cryptographic identity.
- Contacts must be informed about identity changes.

Advanced encrypted history backup and recovery are outside the first beta scope.

## 14. Acceptance Criteria

The design is complete when:

- First-device registration is defined.
- Additional-device authorization is defined.
- Device identity verification is addressed.
- Lost-device handling is documented.
- All-devices-lost recovery is documented.
- Device revocation requirements are defined.
- Backend and frontend responsibilities are separated.
- API requirements are documented.
- Recovery limitations are clearly communicated.
- The design is compatible with future mobile clients, subject to protocol validation.

**Status:** Design documented. Implementation depends on final cryptographic protocol selection and validation.

---

## 15. Final Phase 4 Deliverables

The following design documents now cover the Days 21–28 security design phase:

1. `SECURITY_THREAT_MODEL.md`
2. `E2EE_PROTOCOL_EVALUATION.md`
3. `KEY_LIFECYCLE_DESIGN.md`
4. `DEVICE_REGISTRATION_AND_RECOVERY.md`

These documents establish the security requirements for the next development phases. They do not constitute an independent security audit or proof that the eventual implementation is secure.