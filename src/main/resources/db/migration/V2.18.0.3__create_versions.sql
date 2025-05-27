CREATE TABLE versions (
    version VARCHAR(16) UNIQUE NOT NULL,
    migration_status VARCHAR(20) NOT NULL DEFAULT 'not_required',
    deployed_at BIGINT UNSIGNED DEFAULT 0 NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
