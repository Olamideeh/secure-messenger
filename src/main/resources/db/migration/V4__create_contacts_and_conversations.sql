
-- =========================================
-- 1. CONTACT REQUESTS
-- =========================================

CREATE TABLE contact_requests (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                  sender_id UUID NOT NULL REFERENCES users(id),

                                  recipient_id UUID NOT NULL REFERENCES users(id),

                                  status VARCHAR(20) NOT NULL,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  responded_at TIMESTAMPTZ,

                                  CONSTRAINT chk_different_users
                                      CHECK (sender_id <> recipient_id),

                                  CONSTRAINT chk_contact_request_status
                                      CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))
);


-- Prevent reciprocal or duplicate pending requests.
-- LEAST/GREATEST create a canonical unordered user pair.

CREATE UNIQUE INDEX uq_pending_contact_request_pair
    ON contact_requests (
                         LEAST(sender_id, recipient_id),
                         GREATEST(sender_id, recipient_id)
        )
    WHERE status = 'PENDING';


-- =========================================
-- 2. CONTACTS
-- =========================================

CREATE TABLE contacts (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          user_low_id UUID NOT NULL REFERENCES users(id),

                          user_high_id UUID NOT NULL REFERENCES users(id),

                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_contact_order
                              CHECK (user_low_id < user_high_id),

                          CONSTRAINT uq_contact_pair
                              UNIQUE (user_low_id, user_high_id)
);


-- =========================================
-- 3. CONVERSATIONS
-- =========================================

CREATE TABLE conversations (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               conversation_type VARCHAR(20) NOT NULL,

                               direct_user_low_id UUID REFERENCES users(id),

                               direct_user_high_id UUID REFERENCES users(id),

                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT chk_conversation_type
                                   CHECK (conversation_type IN ('DIRECT', 'GROUP')),

                               CONSTRAINT chk_conversation_users
                                   CHECK (
                                       (
                                           conversation_type = 'DIRECT'
                                               AND direct_user_low_id IS NOT NULL
                                               AND direct_user_high_id IS NOT NULL
                                               AND direct_user_low_id < direct_user_high_id
                                           )
                                           OR
                                       (
                                           conversation_type = 'GROUP'
                                               AND direct_user_low_id IS NULL
                                               AND direct_user_high_id IS NULL
                                           )
                                       )
);


-- Prevent duplicate direct conversations for the same pair.

CREATE UNIQUE INDEX uq_direct_conversation_pair
    ON conversations (direct_user_low_id, direct_user_high_id)
    WHERE conversation_type = 'DIRECT';


-- =========================================
-- 4. CONVERSATION MEMBERS
-- =========================================

CREATE TABLE conversation_members (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                      conversation_id UUID NOT NULL REFERENCES conversations(id),

                                      user_id UUID NOT NULL REFERENCES users(id),

                                      role VARCHAR(20) NOT NULL,

                                      joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      left_at TIMESTAMPTZ,

                                      CONSTRAINT chk_conversation_member_role
                                          CHECK (role IN ('MEMBER', 'ADMIN')),

                                      CONSTRAINT uq_conversation_member
                                          UNIQUE (conversation_id, user_id)
);
