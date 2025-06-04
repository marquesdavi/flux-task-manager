ALTER TABLE cards
    ADD COLUMN assignee_id BIGINT;
ALTER TABLE cards
    ADD CONSTRAINT fk_cards_assignee
        FOREIGN KEY (assignee_id) REFERENCES users(id);
