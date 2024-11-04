CREATE TABLE users (
    id                  BIGSERIAL       PRIMARY KEY,
    username            VARCHAR(32)     NOT NULL UNIQUE,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    password            VARCHAR(255)    NOT NULL,
    status              VARCHAR(64),
    about               VARCHAR(512),
    birth_date          DATE,
    gender              CHAR(1),
    role                VARCHAR(16)     NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP,
    avatar_url          VARCHAR(128)
);


CREATE TABLE book_series (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title               VARCHAR(64)     NOT NULL,
    description         VARCHAR(255),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP,
    status              VARCHAR(16)     NOT NULL
);


CREATE TABLE books (
    id                  BIGSERIAL       PRIMARY KEY,
    title               VARCHAR(64)     NOT NULL,
    form                VARCHAR(16)     NOT NULL,
    status              VARCHAR(16)     NOT NULL,
    price               DECIMAL         NOT NULL,
    annotation          VARCHAR(512)    NOT NULL,
    author_note         VARCHAR(255),
    access_type         VARCHAR(16)     NOT NULL,
    series_id           BIGINT          REFERENCES book_series(id) ON DELETE SET NULL,
    cover_url           VARCHAR(128)
);


CREATE TABLE book_statistics (
    book_id                 BIGINT          PRIMARY KEY REFERENCES books(id) ON DELETE CASCADE,
    pages_count             FLOAT           NOT NULL,
    likes_count             INT             NOT NULL,
    characters_count        INT             NOT NULL,
    ratings_count           INT             NOT NULL,
    rating                  FLOAT,
    views_count             INT             NOT NULL,
    publication_date        TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP,
    library_add_count       INT             NOT NULL,
    reading_now_count       INT             NOT NULL,
    already_read_count      INT             NOT NULL,
    will_read_count         INT             NOT NULL,
    not_interested_count    INT             NOT NULL,
    abandoned_count         INT             NOT NULL,
    downloads_count         INT             NOT NULL
);


CREATE TABLE book_genres (
    book_id     BIGINT          NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    genre       VARCHAR(32)     NOT NULL,
    PRIMARY KEY (book_id, genre)
);


CREATE TABLE book_tags (
    book_id     BIGINT          NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    tag         VARCHAR(32)     NOT NULL,
    PRIMARY KEY (book_id, tag)
);


CREATE TABLE chapters (
    id                  BIGSERIAL       PRIMARY KEY,
    book_id             BIGINT          NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    title               VARCHAR(64)     NOT NULL,
    access_type         VARCHAR(16)     NOT NULL,
    publication_date    TIMESTAMP,
    is_draft            BOOLEAN         NOT NULL,
    content_url         VARCHAR(128)    NOT NULL
);


CREATE TABLE comments (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_id           BIGINT          NOT NULL,
    entity_type         VARCHAR(16)     NOT NULL,
    parent_id           BIGINT          REFERENCES comments(id) ON DELETE CASCADE,
    likes_count         INT             NOT NULL,
    dislikes_count      INT             NOT NULL,
    is_pinned           BOOLEAN         NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP,
    content_url         VARCHAR(128)    NOT NULL
);


CREATE TABLE user_likes (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_id       BIGINT          NOT NULL,
    entity_type     VARCHAR(16)     NOT NULL,
    is_like         BOOLEAN         NOT NULL,
    liked_at        TIMESTAMP       NOT NULL,
    UNIQUE (user_id, entity_id, entity_type)
);


CREATE TABLE book_authors (
    book_id         BIGINT      NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    author_id       BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);


CREATE TABLE user_libraries (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id         BIGINT          NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    section         VARCHAR(16)     NOT NULL,
    addition_date   TIMESTAMP       NOT NULL,
    UNIQUE (user_id, book_id)
);


CREATE TABLE reading_progress (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id             BIGINT          NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    chapter_id          BIGINT          NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    last_read_date      TIMESTAMP       NOT NULL,
    last_read_place     INT             NOT NULL,
    UNIQUE (user_id, book_id)
);


CREATE TABLE book_reviews (
    id                  BIGSERIAL       PRIMARY KEY,
    book_id             BIGINT          NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating              FLOAT,
    views_count         INT             NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP,
    likes_count         INT             NOT NULL,
    dislikes_count      INT             NOT NULL,
    is_spoiler          BOOLEAN         NOT NULL,
    content_url         VARCHAR(128)    NOT NULL,
    UNIQUE (book_id, user_id)
);


CREATE TABLE book_collections (
    id                  BIGSERIAL       PRIMARY KEY,
    title               VARCHAR(64)     NOT NULL,
    description         VARCHAR(255),
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_public           BOOLEAN         NOT NULL,
    is_draft            BOOLEAN         NOT NULL
);


CREATE TABLE book_collection_items (
    id                      BIGSERIAL       PRIMARY KEY,
    book_id                 BIGINT          NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    collection_id           BIGINT          NOT NULL REFERENCES book_collections(id) ON DELETE CASCADE,
    addition_date           TIMESTAMP       NOT NULL,
    user_description        VARCHAR(128),
    UNIQUE (book_id, collection_id)
);


CREATE TABLE book_collection_stats (
    id                  BIGSERIAL       PRIMARY KEY,
    collection_id       BIGINT          REFERENCES book_collections(id) ON DELETE CASCADE,
    likes_count         INT             NOT NULL,
    dislikes_count      INT             NOT NULL,
    rating              FLOAT,
    ratings_count       INT             NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP,
    views_count         INT             NOT NULL
);


CREATE TABLE chapter_statistics (
    id                      BIGSERIAL       PRIMARY KEY,
    chapter_id              BIGINT          NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    views_count             INTEGER         NOT NULL,
    pages_count             FLOAT           NOT NULL,
    characters_count        INT             NOT NULL,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP
);


CREATE TABLE user_ratings (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_id           BIGINT          NOT NULL,
    entity_type         VARCHAR(16)     NOT NULL,
    rating              SMALLINT        NOT NULL,
    rated_at            TIMESTAMP       NOT NULL,
    UNIQUE (user_id, entity_id, entity_type)
);


CREATE TABLE purchased_books (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id             BIGINT          NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    purchase_date       TIMESTAMP       NOT NULL,
    price               DECIMAL         NOT NULL,
    UNIQUE (user_id, book_id)
);


CREATE TABLE book_visits (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id         BIGINT          NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    visit_date      TIMESTAMP       NOT NULL,
    UNIQUE (user_id, book_id)
);


CREATE TABLE subscriptions (
    id                      BIGSERIAL       PRIMARY KEY,
    user_id                 BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_id               BIGINT          NOT NULL,
    entity_type             VARCHAR(16)     NOT NULL,
    notify_on_new_event     BOOLEAN         NOT NULL,
    created_at              TIMESTAMP       NOT NULL,
    UNIQUE (user_id, entity_id, entity_type)
);


CREATE TABLE notifications (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_id       BIGINT          NOT NULL,
    entity_type     VARCHAR(16)     NOT NULL,
    type            VARCHAR(32)     NOT NULL,
    is_read         BOOLEAN         NOT NULL,
    created_at      TIMESTAMP       NOT NULL
);


CREATE TABLE refresh_tokens (
    jti                 UUID            PRIMARY KEY,
    device_id           VARCHAR(128)    NOT NULL,
    is_revoked          BOOLEAN         NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    expires_at          TIMESTAMP       NOT NULL,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE
);


CREATE INDEX idx_book_series_user_id ON book_series (user_id);
CREATE INDEX idx_books_series_id ON books (series_id);
CREATE INDEX idx_chapters_book_id ON chapters (book_id);
CREATE INDEX idx_comments_user_id ON comments (user_id);
CREATE INDEX idx_comments_parent_id ON comments (parent_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_chapter_statistics_chapter_id ON chapter_statistics (chapter_id);
CREATE INDEX idx_book_collection_stats_collection_id ON book_collection_stats (collection_id);
CREATE INDEX idx_book_collections_user_id ON book_collections (user_id);
