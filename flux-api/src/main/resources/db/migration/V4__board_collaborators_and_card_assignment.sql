CREATE TABLE board_collaborators (
    board_id UUID      NOT NULL,
    user_id  BIGINT    NOT NULL,
    CONSTRAINT pk_board_collaborators PRIMARY KEY (board_id, user_id),
    CONSTRAINT fk_bc_board
        FOREIGN KEY (board_id)
        REFERENCES boards (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bc_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

ALTER TABLE cards
ADD COLUMN assigned_user_id BIGINT;

ALTER TABLE cards
ADD CONSTRAINT fk_cards_assigned_user
    FOREIGN KEY (assigned_user_id)
    REFERENCES users (id)
    ON DELETE SET NULL;

CREATE INDEX idx_cards_assigned_user ON cards (assigned_user_id);

CREATE INDEX idx_bc_user ON board_collaborators (user_id);
