CREATE TABLE IF NOT EXISTS auth_password_action (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    expiration_time TIMESTAMP
);
