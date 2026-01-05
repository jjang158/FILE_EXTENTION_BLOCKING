#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create appuser if not exists
    DO
    \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '$APP_USER') THEN
            CREATE USER $APP_USER WITH PASSWORD '$APP_PASSWORD';
        END IF;
    END
    \$\$;

    -- Grant database privileges
    GRANT ALL PRIVILEGES ON DATABASE $POSTGRES_DB TO $APP_USER;

    -- Grant schema privileges
    GRANT ALL PRIVILEGES ON SCHEMA public TO $APP_USER;
    GRANT USAGE ON SCHEMA public TO $APP_USER;

    -- Extension Policy Table
    CREATE TABLE IF NOT EXISTS extension_policy (
        id BIGSERIAL PRIMARY KEY,
        namespace VARCHAR(50) NOT NULL UNIQUE,
        status CHAR(1) DEFAULT 'Y',
        description VARCHAR(255)
    );

    -- Extension Rule Table
    CREATE TABLE IF NOT EXISTS extension_rule (
        id BIGSERIAL PRIMARY KEY,
        policy_id BIGINT NOT NULL,
        extension VARCHAR(20) NOT NULL,
        type VARCHAR(10) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_policy FOREIGN KEY (policy_id) REFERENCES extension_policy(id) ON DELETE CASCADE,
        CONSTRAINT uk_policy_extension UNIQUE (policy_id, extension)
    );

    -- Set table ownership to appuser
    ALTER TABLE extension_policy OWNER TO $APP_USER;
    ALTER TABLE extension_rule OWNER TO $APP_USER;

    -- Set sequence ownership to appuser
    ALTER SEQUENCE extension_policy_id_seq OWNER TO $APP_USER;
    ALTER SEQUENCE extension_rule_id_seq OWNER TO $APP_USER;

    -- Grant all privileges on tables to appuser
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $APP_USER;

    -- Grant all privileges on sequences to appuser
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $APP_USER;

    -- Grant default privileges for future objects
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO $APP_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO $APP_USER;
EOSQL
