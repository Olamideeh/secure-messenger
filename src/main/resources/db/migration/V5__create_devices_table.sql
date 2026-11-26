
CREATE TABLE devices (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         user_id UUID NOT NULL REFERENCES users(id),

                         device_name VARCHAR(100) NOT NULL,

                         platform VARCHAR(20) NOT NULL,

                         status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         approved_at TIMESTAMPTZ NULL,

                         last_seen_at TIMESTAMPTZ NULL,

                         revoked_at TIMESTAMPTZ NULL,

                         CONSTRAINT chk_devices_platform
                             CHECK (platform IN ('WEB', 'ANDROID', 'IOS')),

                         CONSTRAINT chk_devices_status
                             CHECK (status IN ('PENDING', 'ACTIVE', 'REVOKED')),

                         CONSTRAINT chk_devices_lifecycle
                             CHECK (
                                 (status = 'PENDING'
                                     AND approved_at IS NULL
                                     AND revoked_at IS NULL)

                                     OR

                                 (status = 'ACTIVE'
                                     AND approved_at IS NOT NULL
                                     AND revoked_at IS NULL)

                                     OR

                                 (status = 'REVOKED'
                                     AND approved_at IS NOT NULL
                                     AND revoked_at IS NOT NULL)
                                 )
);

CREATE INDEX idx_devices_user_id
    ON devices(user_id);
