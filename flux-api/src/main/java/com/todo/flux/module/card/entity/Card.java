package com.todo.flux.module.card.entity;

import com.todo.flux.module.board.entity.Board;
import com.todo.flux.module.card.dto.CardCreateRequest;
import com.todo.flux.module.card.dto.CardResponse;
import com.todo.flux.module.card.dto.CardUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column
    private String descriptionBrief;

    @Column(columnDefinition = "TEXT")
    private String descriptionFull;

    @Column(nullable = false)
    private String status;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate dueDate;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Card fromRequest(CardCreateRequest dto, Board board) {
        return Card.builder()
                .title(dto.title())
                .descriptionBrief(dto.descriptionBrief())
                .descriptionFull(dto.descriptionFull())
                .status(dto.status())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .dueDate(dto.dueDate())
                .imageUrl(dto.imageUrl())
                .board(board)
                .build();
    }

    public void applyUpdate(CardUpdateRequest dto) {
        this.setTitle(dto.title());
        this.setDescriptionBrief(dto.descriptionBrief());
        this.setDescriptionFull(dto.descriptionFull());
        this.setStatus(dto.status());
        this.setStartDate(dto.startDate());
        this.setEndDate(dto.endDate());
        this.setDueDate(dto.dueDate());
        this.setImageUrl(dto.imageUrl());
    }

    public CardResponse toResponse() {
        return new CardResponse(
                this.getId(),
                this.getTitle(),
                this.getDescriptionBrief(),
                this.getDescriptionFull(),
                this.getStatus(),
                this.getStartDate(),
                this.getEndDate(),
                this.getDueDate(),
                this.getImageUrl(),
                this.getBoard().getId(),
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }
}
