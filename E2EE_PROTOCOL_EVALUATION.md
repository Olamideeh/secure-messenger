# Secure Messenger — E2EE Protocol Evaluation

**Project:** Secure Messenger  
**Phase:** Device Identity & E2EE Design  
**Evaluation period:** Days 22–23  
**Status:** Initial evaluation — final protocol selection requires technical validation

## 1. Purpose

Secure Messenger requires end-to-end encryption for private messaging.

The encryption system must support:

- React and TypeScript web clients.
- Future Android and iOS applications.
- Multiple devices belonging to one user.
- Secure identity and key management.
- Forward secrecy and protection after key compromise.
- Long-term maintenance and security updates.

The backend will use Java and Spring Boot. It must route and store encrypted message data without being able to decrypt message contents.

We will evaluate established cryptographic protocols rather than develop our own.

## 2. Candidates

Two candidates are considered:

1. Signal Protocol implementation — libsignal.
2. Messaging Layer Security — MLS, using OpenMLS as a possible implementation.

These are not identical solutions. Signal Protocol and MLS have different protocol designs and implementation ecosystems.

## 3. Candidate A — Signal Protocol (libsignal)

### Overview

The Signal Protocol is an established end-to-end encryption protocol family used for secure messaging.

Its cryptographic components include:

- X3DH and newer PQXDH key agreement approaches.
- Double Ratchet for evolving message encryption keys.
- Sesame concepts for asynchronous multi-device messaging.

The libsignal project provides implementations and language bindings, including Java, Swift and TypeScript-related support.

### Evaluation

| Requirement | Assessment |
|---|---|
| React and TypeScript | Potentially suitable, but browser support and packaging must be verified |
| Android compatibility | Strong candidate through supported native integration |
| iOS compatibility | Strong candidate through Swift integration |
| One-to-one messaging | Suitable |
| Multiple devices | Supported by the broader protocol design, but application integration is complex |
| Key generation and exchange | Supported by protocol components |
| Forward secrecy | Provided through ratcheting mechanisms |
| Post-compromise security | Supported through ratchet recovery properties |
| Maintenance | Active project, but external integration support and API stability are concerns |
| Spring Boot integration | Java implementation may be useful, but server-side protocol responsibilities must be carefully separated from client encryption |

### Advantages

- Mature and widely recognized protocol family.
- Strong design for asynchronous encrypted messaging.
- Suitable conceptual foundation for one-to-one messaging.
- Has implementations for multiple programming environments.

### Limitations

- Signal's libsignal repository states that use outside Signal is unsupported.
- APIs and internal interfaces may change.
- Browser integration must be verified rather than assumed from TypeScript support.
- Multi-device support requires careful application-level implementation.
- Licensing and distribution requirements must be reviewed before adoption.

### Initial assessment

**Promising candidate, but not yet approved for production use.**

The browser implementation and external integration risks require further investigation.

## 4. Candidate B — MLS with OpenMLS

### Overview

Messaging Layer Security (MLS) is a standardized protocol described in RFC 9420.

It is designed to provide asynchronous group key establishment and secure group messaging.

OpenMLS is a Rust implementation of the MLS protocol.

### Evaluation

| Requirement | Assessment |
|---|---|
| React and TypeScript | Possible through WebAssembly or a compatible wrapper, subject to validation |
| Android compatibility | Possible through Rust/native integration |
| iOS compatibility | Possible through Rust/native integration |
| One-to-one messaging | Can be modeled using MLS groups, but application design is required |
| Multiple devices | Can be modeled through group membership and credential management |
| Key generation and exchange | Defined by MLS |
| Forward secrecy | Supported by MLS epoch and key-update mechanisms |
| Post-compromise security | Supported through protocol updates and fresh key material |
| Maintenance | Project maturity and maintenance status must be checked before adoption |
| Spring Boot integration | Backend can handle encrypted MLS messages and associated metadata without possessing client secrets |

### Advantages

- Based on an IETF-standardized protocol.
- Designed for secure group communication.
- Has a Rust implementation.
- Offers a potential path for shared cryptographic code across platforms.

### Limitations

- Browser support requires additional integration work.
- WebAssembly integration and secure key storage need investigation.
- Android and iOS integration require native platform work.
- MLS introduces concepts such as groups, epochs, credentials and proposals that require careful application design.
- It may be more complex than necessary for the initial one-to-one messaging beta.

### Initial assessment

**Technically interesting candidate, but requires further investigation.**

Browser integration, implementation maturity and the complexity of using MLS for our initial messaging scope must be evaluated.

## 5. Comparison

| Area | Signal Protocol | MLS / OpenMLS |
|---|---|---|
| Primary design | Secure asynchronous messaging | Secure group messaging |
| One-to-one messaging | Natural fit | Possible through group modeling |
| Group messaging | Supported through related protocol designs | Core protocol capability |
| Browser integration | Requires verification | Likely requires WebAssembly integration |
| Mobile integration | Native implementation options | Native Rust integration possible |
| Key management | Ratchets and identity/prekeys | Credentials, epochs and group key schedule |
| Integration complexity | Significant | Significant |
| Main concern | External support and API stability | Browser integration and application complexity |

## 6. Security Requirements Regardless of Selection

Whichever protocol is selected, Secure Messenger must:

1. Generate private keys on client devices.
2. Protect private keys using appropriate platform security facilities.
3. Never send private keys to the Spring Boot backend.
4. Authenticate public keys and device identities.
5. Support secure device registration and revocation.
6. Protect message confidentiality and integrity.
7. Handle key rotation and identity changes.
8. Avoid logging plaintext messages or cryptographic secrets.
9. Support multiple devices without weakening the security model.
10. Use established, maintained cryptographic implementations.

## 7. Provisional Recommendation

For the initial Secure Messenger release, Signal Protocol is the first candidate to investigate more deeply because its design is closely aligned with asynchronous one-to-one secure messaging.

However, we must not adopt libsignal until we have verified:

- A practical and maintained browser implementation.
- Compatibility with React and TypeScript.
- Licensing and distribution requirements.
- A realistic integration path for future Android and iOS clients.
- Multi-device behavior and key lifecycle requirements.
- Whether its external-use restrictions are acceptable for our product.

OpenMLS remains an alternative worth considering, especially if group messaging becomes a major requirement.

**No cryptographic implementation is approved at this stage.**

## 8. Outstanding Decisions

The following questions must be answered before implementation:

- Which exact library and version will be used?
- How will browser private keys be stored?
- How will devices exchange and verify identity information?
- How will new devices join an existing account?
- How will key rotation work?
- How will a lost device be revoked?
- What happens to encrypted message history after account recovery?
- How will the chosen protocol work across web, Android and iOS?

## 9. Conclusion

Both Signal Protocol and MLS provide established approaches to end-to-end encrypted communication.

Signal Protocol is currently the leading candidate for our initial one-to-one messaging requirements, while MLS remains a possible alternative.

The final decision must be based on verified platform compatibility, security properties, maintenance, licensing and implementation feasibility.

Secure Messenger will not implement custom cryptography or claim production-ready E2EE until the selected implementation has been properly integrated and tested.