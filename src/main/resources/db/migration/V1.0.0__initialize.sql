/* TODO: have to consider column, column-length, multiple-column indexes, foreign key etc...  */
CREATE TABLE IF NOT EXISTS authors (
    id CHAR(26) CHARACTER SET ascii UNIQUE NOT NULL,
    name VARCHAR(32) UNIQUE NOT NULL,
    display_name VARCHAR(32),
    password VARCHAR(72) NOT NULL,
    created_at BIGINT UNSIGNED DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS content_types (
    id CHAR(26) CHARACTER SET ascii UNIQUE NOT NULL,
    name VARCHAR(32) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS contents (
    id CHAR(26) CHARACTER SET ascii UNIQUE NOT NULL,
    author_id CHAR(26) CHARACTER SET ascii NOT NULL,
    content_type_id CHAR(26) CHARACTER SET ascii NOT NULL,
    path VARCHAR(512) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    raw_content MEDIUMTEXT NOT NULL,
    html_content MEDIUMTEXT NOT NULL,
    published_at BIGINT UNSIGNED DEFAULT 0,
    updated_at BIGINT UNSIGNED DEFAULT 0,
    FOREIGN KEY fk_author(author_id) REFERENCES authors(id),
    FOREIGN KEY fk_content_type(content_type_id) REFERENCES content_types(id),
    FULLTEXT idx_fulltext(raw_content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# https://developers.google.com/search/docs/advanced/robots/robots_meta_tag
CREATE TABLE IF NOT EXISTS robots (
    content_id CHAR(26) CHARACTER SET ascii UNIQUE NOT NULL,
    attributes VARCHAR(128) NOT NULL,
    FOREIGN KEY fk_content(content_id) REFERENCES contents(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS external_resources (
    content_id CHAR(26) CHARACTER SET ascii NOT NULL,
    kind VARCHAR(32) NOT NULL,
    name VARCHAR(32) NOT NULL,
    PRIMARY KEY (content_id, kind, name),
    FOREIGN KEY fk_content_from_external_resources(content_id) REFERENCES contents(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tags(
    id CHAR(26) CHARACTER SET ascii UNIQUE NOT NULL,
    name VARCHAR(32) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS contents_tagging(
    content_id CHAR(26) CHARACTER SET ascii NOT NULL,
    tag_id CHAR(26) CHARACTER SET ascii NOT NULL,
    PRIMARY KEY (content_id, tag_id),
    FOREIGN KEY fk_content_from_contents_tagging(content_id) REFERENCES contents(id),
    FOREIGN KEY fk_tag_from_contents_tagging(tag_id) REFERENCES tags(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

