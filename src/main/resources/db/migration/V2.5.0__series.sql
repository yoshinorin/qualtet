CREATE TABLE IF NOT EXISTS series (
    id CHAR(26) CHARACTER SET ascii PRIMARY KEY NOT NULL,
    path VARCHAR(512) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1024) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS series_contents_belonging (
    series_id CHAR(26) CHARACTER SET ascii NOT NULL,
    content_id CHAR(26) CHARACTER SET ascii NOT NULL,
    PRIMARY KEY (series_id, content_id),
    FOREIGN KEY fk_series_from_series_contents_belonging(series_id) REFERENCES series(id),
    FOREIGN KEY fk_content_from_series_contents_belonging(content_id) REFERENCES contents(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

