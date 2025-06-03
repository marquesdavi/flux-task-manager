CREATE TABLE IF NOT EXISTS cards (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description_brief VARCHAR(255),
    description_full TEXT,
    status VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    due_date DATE,
    image_url TEXT,
    board_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cards_board
    FOREIGN KEY(board_id)
    REFERENCES boards(id)
    ON DELETE CASCADE
);
