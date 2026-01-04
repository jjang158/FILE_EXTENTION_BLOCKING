-- Create appuser if not exists (conditional creation)
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'appuser') THEN
        CREATE USER appuser WITH PASSWORD 'apppassword';
    END IF;
END
$$;

-- Grant database privileges
GRANT ALL PRIVILEGES ON DATABASE extension_blocker TO appuser;

-- Grant schema privileges
GRANT ALL PRIVILEGES ON SCHEMA public TO appuser;
GRANT USAGE ON SCHEMA public TO appuser;

-- Extension Policy Table
CREATE TABLE IF NOT EXISTS extension_policy (
    id BIGSERIAL PRIMARY KEY,
    namespace VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Extension Rule Table
CREATE TABLE IF NOT EXISTS extension_rule (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    extension VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_policy FOREIGN KEY (policy_id) REFERENCES extension_policy(id) ON DELETE CASCADE,
    CONSTRAINT uk_policy_extension UNIQUE (policy_id, extension)
);

-- Set table ownership to appuser
ALTER TABLE extension_policy OWNER TO appuser;
ALTER TABLE extension_rule OWNER TO appuser;

-- Set sequence ownership to appuser
ALTER SEQUENCE extension_policy_id_seq OWNER TO appuser;
ALTER SEQUENCE extension_rule_id_seq OWNER TO appuser;

-- Grant all privileges on tables to appuser
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO appuser;

-- Grant all privileges on sequences to appuser
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO appuser;

-- Grant default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO appuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO appuser;
