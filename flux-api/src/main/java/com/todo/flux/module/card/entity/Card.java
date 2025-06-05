package com.todo.flux.module.card.entity;

import com.todo.flux.module.board.entity.Board;
import com.todo.flux.module.card.dto.CardCreateRequest;
import com.todo.flux.module.card.dto.CardResponse;
import com.todo.flux.module.card.dto.CardUpdateRequest;
import com.todo.flux.module.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "default 'LOW'")
    private CardPriority priority;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate dueDate;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

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
                .status(CardStatus.valueOf(dto.status()))
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
        this.setStatus(CardStatus.valueOf(dto.status()));
        if (dto.priority() == null) {
            this.setPriority(CardPriority.LOW);
        } else {
            this.setPriority(CardPriority.valueOf(dto.priority()));
        }
        this.setStartDate(dto.startDate());
        this.setEndDate(dto.endDate());
        this.setDueDate(dto.dueDate());
        this.setImageUrl(dto.imageUrl());
    }

    public void assignTo(User user) {
        this.assignee = user;
    }

    public CardResponse toResponse() {
        return new CardResponse(
                this.getId(),
                this.getTitle(),
                this.getDescriptionBrief(),
                this.getDescriptionFull(),
                this.getStatus().name(),
                this.getStartDate(),
                this.getEndDate(),
                this.getDueDate(),
                this.getImageUrl(),
                this.getBoard().getId(),
                Objects.nonNull(this.getPriority()) ? this.getPriority().name() : null,
                Objects.nonNull(this.getAssignee()) ? this.getAssignee().getEmail() : null,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }
}
