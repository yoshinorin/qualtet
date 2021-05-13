/* TODO: have to consider column, multiple-column indexes, foreign key etc...  */
CREATE TABLE IF NOT EXISTS authors (
    uuid CHAR(36) CHARACTER SET ascii UNIQUE NOT NULL,
    name VARCHAR(32) UNIQUE NOT NULL,
    display_name VARCHAR(32),
    created_at BIGINT UNSIGNED DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE content_types (
    uuid CHAR(36) CHARACTER SET ascii UNIQUE NOT NULL,
    name VARCHAR(32) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS contents (
    uuid CHAR(36) CHARACTER SET ascii UNIQUE NOT NULL,
    author_id CHAR(36) CHARACTER SET ascii NOT NULL,
    content_type_id CHAR(36) CHARACTER SET ascii NOT NULL,
    path VARCHAR(512) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    raw_content MEDIUMTEXT NOT NULL,
    html_content MEDIUMTEXT NOT NULL,
    published_at BIGINT UNSIGNED DEFAULT 0,
    updated_at BIGINT UNSIGNED DEFAULT 0,
    FOREIGN KEY fk_author(author_id) REFERENCES authors(uuid),
    FOREIGN KEY fk_content_type(content_type_id) REFERENCES content_types(uuid),
    FULLTEXT idx_fulltext(raw_content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


INSERT INTO content_types VALUES (UUID(), 'article');
INSERT INTO content_types VALUES (UUID(), 'page');
