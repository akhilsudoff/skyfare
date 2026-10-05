CREATE TABLE app_user (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE watch (
    id            UUID PRIMARY KEY,
    user_id       UUID          NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    origin        VARCHAR(3)    NOT NULL,
    destination   VARCHAR(3)    NOT NULL,
    depart_date   DATE          NOT NULL,
    target_price  NUMERIC(10,2) NOT NULL,
    last_price    NUMERIC(10,2),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_watch UNIQUE (user_id, origin, destination, depart_date)
);
CREATE INDEX idx_watch_user ON watch (user_id);

CREATE TABLE notification (
    id          UUID PRIMARY KEY,
    user_id     UUID          NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    watch_id    UUID          NOT NULL REFERENCES watch (id) ON DELETE CASCADE,
    message     TEXT          NOT NULL,
    old_price   NUMERIC(10,2),
    new_price   NUMERIC(10,2),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_notification_user_created ON notification (user_id, created_at DESC);
