CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(16) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_users_role CHECK ( role IN ('USER', 'ADMIN') ),
    CONSTRAINT ck_users_phone_format CHECK ( phone ~ '^\+[1-9][0-9]{7,14}$' )
);

CREATE UNIQUE INDEX ux_users_email_lower ON users (lower(email));
CREATE UNIQUE INDEX ux_users_phone ON users (phone);

CREATE TABLE refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    user_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL ,
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash)
);