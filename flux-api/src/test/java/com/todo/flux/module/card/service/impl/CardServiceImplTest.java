package com.todo.flux.module.card.service.impl;

import com.todo.flux.exception.NotFoundException;
import com.todo.flux.module.auth.dto.LoginRequest;
import com.todo.flux.module.auth.dto.TokenResponse;
import com.todo.flux.module.auth.service.AuthenticationService;
import com.todo.flux.module.board.entity.Board;
import com.todo.flux.module.board.service.BoardService;
import com.todo.flux.module.card.dto.CardCreateRequest;
import com.todo.flux.module.card.dto.CardResponse;
import com.todo.flux.module.card.dto.CardUpdateRequest;
import com.todo.flux.module.card.entity.Card;
import com.todo.flux.module.card.entity.CardStatus;
import com.todo.flux.module.card.repository.CardRepository;
import com.todo.flux.module.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardServiceImpl Tests")
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardService boardService;

    @Mock
    private AuthenticationService<User, LoginRequest, TokenResponse> authService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User currentUser;
    private User otherUser;
    private Board boardOwnedByCurrentUser;
    private Board boardOwnedByOtherUser;
    private Card cardOnCurrentUserBoard;
    private Card cardOnOtherUserBoard;

    private Long currentUserId;
    private Long otherUserId;
    private UUID boardOwnedByCurrentUserId;
    private UUID boardOwnedByOtherUserId;
    private UUID cardOnCurrentUserBoardId;
    private UUID cardOnOtherUserBoardId;

    @BeforeEach
    void setUp() {
        currentUserId = RandomGenerator.getDefault().nextLong();
        currentUser = User.builder().id(currentUserId).build();

        otherUserId = RandomGenerator.getDefault().nextLong();
        otherUser = User.builder().id(otherUserId).build();

        boardOwnedByCurrentUserId = UUID.randomUUID();
        boardOwnedByCurrentUser = Board.builder()
                .id(boardOwnedByCurrentUserId)
                .owner(currentUser)
                .title("Current User's Board")
                .build();

        boardOwnedByOtherUserId = UUID.randomUUID();
        boardOwnedByOtherUser = Board.builder()
                .id(boardOwnedByOtherUserId)
                .owner(otherUser)
                .title("Other User's Board")
                .build();

        cardOnCurrentUserBoardId = UUID.randomUUID();
        cardOnCurrentUserBoard = Card.builder()
                .id(cardOnCurrentUserBoardId)
                .title("Card on Current User's Board")
                .status(CardStatus.valueOf("TODO"))
                .board(boardOwnedByCurrentUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        cardOnOtherUserBoardId = UUID.randomUUID();
        cardOnOtherUserBoard = Card.builder()
                .id(cardOnOtherUserBoardId)
                .title("Card on Other User's Board")
                .status(CardStatus.valueOf("TODO"))
                .board(boardOwnedByOtherUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        lenient().when(authService.getAuthenticated()).thenReturn(currentUser);
    }

    @Nested
    @DisplayName("create method tests")
    class Create {

        private CardCreateRequest createRequest;

        @BeforeEach
        void createSetUp() {
            createRequest = new CardCreateRequest(
                    "New Card Title",
                    "Brief desc",
                    "Full desc",
                    "TODO",
                    LocalDate.now(),
                    null,
                    LocalDate.now().plusDays(7),
                    "http://image.url/new.png"
            );
        }

        @Test
        @DisplayName("should create card when board exists and user is owner")
        void shouldCreateCard_whenBoardExistsAndUserIsOwner() {
            when(boardService.findById(boardOwnedByCurrentUserId)).thenReturn(boardOwnedByCurrentUser);
            Card cardToSave = Card.fromRequest(createRequest, boardOwnedByCurrentUser);

            Card savedCard = Card.builder()
                    .id(UUID.randomUUID())
                    .title(cardToSave.getTitle())
                    .descriptionBrief(cardToSave.getDescriptionBrief())
                    .descriptionFull(cardToSave.getDescriptionFull())
                    .status(cardToSave.getStatus())
                    .startDate(cardToSave.getStartDate())
                    .endDate(cardToSave.getEndDate())
                    .dueDate(cardToSave.getDueDate())
                    .imageUrl(cardToSave.getImageUrl())
                    .board(boardOwnedByCurrentUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
            
            CardResponse actualResponse = cardService.create(boardOwnedByCurrentUserId, createRequest);

            assertNotNull(actualResponse);
            assertEquals(savedCard.getId(), actualResponse.id());
            assertEquals(createRequest.title(), actualResponse.title());
            assertEquals(createRequest.status(), actualResponse.status());
            assertEquals(boardOwnedByCurrentUserId, actualResponse.boardId());

            verify(authService, times(1)).getAuthenticated();
            verify(boardService, times(1)).findById(boardOwnedByCurrentUserId);
            verify(cardRepository, times(1)).save(any(Card.class));
        }

        @Test
        @DisplayName("should throw NotFoundException when board not found for user")
        void shouldThrowNotFoundException_whenBoardNotFoundForUser() {
            when(boardService.findById(boardOwnedByOtherUserId)).thenReturn(boardOwnedByOtherUser);
            
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.create(boardOwnedByOtherUserId, createRequest));

            assertEquals("Board not found for this user", exception.getMessage());
            verify(authService, times(1)).getAuthenticated();
            verify(boardService, times(1)).findById(boardOwnedByOtherUserId);
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("should propagate NotFoundException from BoardService.findById")
        void shouldPropagateNotFoundException_fromBoardService() {
            UUID nonExistentBoardId = UUID.randomUUID();
            when(boardService.findById(nonExistentBoardId)).thenThrow(new NotFoundException("Board not found by ID"));

            
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.create(nonExistentBoardId, createRequest));

            assertEquals("Board not found by ID", exception.getMessage());
            verify(authService, times(1)).getAuthenticated();
            verify(boardService, times(1)).findById(nonExistentBoardId);
            verify(cardRepository, never()).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("listByBoard method tests")
    class ListByBoard {

        @Test
        @DisplayName("should return cards when board exists and user is owner")
        void shouldReturnCards_whenBoardExistsAndUserIsOwner() {
            Card anotherCard = Card.builder()
                    .id(UUID.randomUUID())
                    .title("Another Card")
                    .status(CardStatus.valueOf("DOING"))
                    .board(boardOwnedByCurrentUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            List<Card> cardsOnBoard = List.of(cardOnCurrentUserBoard, anotherCard);

            when(boardService.findById(boardOwnedByCurrentUserId)).thenReturn(boardOwnedByCurrentUser);
            when(cardRepository.findByBoardId(boardOwnedByCurrentUserId)).thenReturn(cardsOnBoard);
            
            List<CardResponse> actualResponses = cardService.listByBoard(boardOwnedByCurrentUserId);

            assertNotNull(actualResponses);
            assertEquals(2, actualResponses.size());
            assertEquals(cardOnCurrentUserBoard.getId(), actualResponses.get(0).id());
            assertEquals(anotherCard.getId(), actualResponses.get(1).id());

            verify(authService, times(1)).getAuthenticated();
            verify(boardService, times(1)).findById(boardOwnedByCurrentUserId);
            verify(cardRepository, times(1)).findByBoardId(boardOwnedByCurrentUserId);
        }

        @Test
        @DisplayName("should return empty list when no cards found for board")
        void shouldReturnEmptyList_whenNoCardsFoundForBoard() {
            when(boardService.findById(boardOwnedByCurrentUserId)).thenReturn(boardOwnedByCurrentUser);
            when(cardRepository.findByBoardId(boardOwnedByCurrentUserId)).thenReturn(Collections.emptyList());
            
            List<CardResponse> actualResponses = cardService.listByBoard(boardOwnedByCurrentUserId);
            
            assertNotNull(actualResponses);
            assertTrue(actualResponses.isEmpty());

            verify(authService, times(1)).getAuthenticated();
            verify(boardService, times(1)).findById(boardOwnedByCurrentUserId);
            verify(cardRepository, times(1)).findByBoardId(boardOwnedByCurrentUserId);
        }


        @Test
        @DisplayName("should throw NotFoundException when board not found for user")
        void shouldThrowNotFoundException_whenBoardNotFoundForUser() {
            when(boardService.findById(boardOwnedByOtherUserId)).thenReturn(boardOwnedByOtherUser);
            
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.listByBoard(boardOwnedByOtherUserId));

            assertEquals("Board not found for this user", exception.getMessage());
            verify(authService, times(1)).getAuthenticated();
            verify(boardService, times(1)).findById(boardOwnedByOtherUserId);
            verify(cardRepository, never()).findByBoardId(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("getById method tests")
    class GetById {

        @Test
        @DisplayName("should return card when card exists and user is owner of the board")
        void shouldReturnCard_whenCardExistsAndUserIsOwner() {
            when(cardRepository.findById(cardOnCurrentUserBoardId)).thenReturn(Optional.of(cardOnCurrentUserBoard));

            CardResponse actualResponse = cardService.getById(cardOnCurrentUserBoardId);
            
            assertNotNull(actualResponse);
            assertEquals(cardOnCurrentUserBoard.getId(), actualResponse.id());
            assertEquals(cardOnCurrentUserBoard.getTitle(), actualResponse.title());

            verify(authService, times(1)).getAuthenticated();
            verify(cardRepository, times(1)).findById(cardOnCurrentUserBoardId);
        }

        @Test
        @DisplayName("should throw NotFoundException when card not found by repository")
        void shouldThrowNotFoundException_whenCardNotFoundByRepository() {
            UUID nonExistentCardId = UUID.randomUUID();
            when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());
            
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.getById(nonExistentCardId));

            assertEquals("Card not found", exception.getMessage());
            verify(authService, times(1)).getAuthenticated();
            verify(cardRepository, times(1)).findById(nonExistentCardId);
        }

        @Test
        @DisplayName("should throw NotFoundException when card exists but user is not owner of the board")
        void shouldThrowNotFoundException_whenCardExistsButUserIsNotOwner() {
            when(cardRepository.findById(cardOnOtherUserBoardId)).thenReturn(Optional.of(cardOnOtherUserBoard));

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.getById(cardOnOtherUserBoardId));

            assertEquals("Card not found for this user", exception.getMessage());
            verify(authService, times(1)).getAuthenticated();
            verify(cardRepository, times(1)).findById(cardOnOtherUserBoardId);
        }
    }

    @Nested
    @DisplayName("update method tests")
    class Update {

        private CardUpdateRequest updateRequest;

        @BeforeEach
        void updateSetUp() {
            updateRequest = new CardUpdateRequest(
                    "Updated Card Title",
                    "Updated brief desc",
                    "Updated full desc",
                    "DOING",
                    "MEDIUM",
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(2),
                    LocalDate.now().plusDays(5),
                    "http://image.url/updated.png"
            );
        }

        @Test
        @DisplayName("should update card when card exists and user is owner of the board")
        void shouldUpdateCard_whenCardExistsAndUserIsOwner() {
            when(cardRepository.findById(cardOnCurrentUserBoardId)).thenReturn(Optional.of(cardOnCurrentUserBoard));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            CardResponse actualResponse = cardService.update(cardOnCurrentUserBoardId, updateRequest);

            assertNotNull(actualResponse);
            assertEquals(cardOnCurrentUserBoardId, actualResponse.id());
            assertEquals(updateRequest.title(), actualResponse.title());
            assertEquals(updateRequest.descriptionBrief(), actualResponse.descriptionBrief());
            assertEquals(updateRequest.status(), actualResponse.status());

            verify(authService, times(1)).getAuthenticated();
            verify(cardRepository, times(1)).findById(cardOnCurrentUserBoardId);
            verify(cardRepository, times(1)).save(cardOnCurrentUserBoard); // Verify the instance was saved

            assertEquals(updateRequest.title(), cardOnCurrentUserBoard.getTitle());
            assertEquals(updateRequest.descriptionFull(), cardOnCurrentUserBoard.getDescriptionFull());
        }

        @Test
        @DisplayName("should throw NotFoundException when card to update is not found by repository")
        void shouldThrowNotFoundException_whenCardToUpdateNotFoundByRepository() {
            
            UUID nonExistentCardId = UUID.randomUUID();
            when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

            
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.update(nonExistentCardId, updateRequest));

            assertEquals("Card not found", exception.getMessage());
            verify(authService, times(1)).getAuthenticated();
            verify(cardRepository, times(1)).findById(nonExistentCardId);
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("should throw NotFoundException when card exists but user is not owner of the board")
        void shouldThrowNotFoundException_whenCardExistsButUserIsNotOwner() {
            
            when(cardRepository.findById(cardOnOtherUserBoardId)).thenReturn(Optional.of(cardOnOtherUserBoard));

            
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.update(cardOnOtherUserBoardId, updateRequest));

            assertEquals("Card not found for this user", exception.getMessage());
            verify(authService, times(1)).getAuthenticated();
            verify(cardRepository, times(1)).findById(cardOnOtherUserBoardId);
            verify(cardRepository, never()).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("delete method tests")
    class Delete {

        @Test
        @DisplayName("should delete card when card exists and user is owner of the board")
        void shouldDeleteCard_whenCardExistsAndUserIsOwner() {
            
            when(cardRepository.findById(cardOnCurrentUserBoardId)).thenReturn(Optional.of(cardOnCurrentUserBoard));
            doNothing().when(cardRepository).delete(cardOnCurrentUserBoard);

            
            assertDoesNotThrow(() -> cardService.delete(cardOnCurrentUserBoardId));

            verify(authService, times(1)).getAuthenticated();
            verify(cardRepository, times(1)).findById(cardOnCurrentUserBoardId);
            verify(cardRepository, times(1)).delete(cardOnCurrentUserBoard);
        }

        @Test
        @DisplayName("should throw NotFoundException when card to delete is not found by repository")
        void shouldThrowNotFoundException_whenCardToDeleteNotFoundByRepository() {
            
            UUID nonExistentCardId = UUID.randomUUID();
            when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

            
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.delete(nonExistentCardId));

            assertEquals("Card not found", exception.getMessage());
            verify(authService, times(1)).getAuthenticated();
            verify(cardRepository, times(1)).findById(nonExistentCardId);
            verify(cardRepository, never()).delete(any(Card.class));
        }

        @Test
        @DisplayName("should throw NotFoundException when card exists but user is not owner of the board")
        void shouldThrowNotFoundException_whenCardExistsButUserIsNotOwner() {
            
            when(cardRepository.findById(cardOnOtherUserBoardId)).thenReturn(Optional.of(cardOnOtherUserBoard));

            
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.delete(cardOnOtherUserBoardId));

            assertEquals("Card not found for this user", exception.getMessage());
            verify(authService, times(1)).getAuthenticated();
            verify(cardRepository, times(1)).findById(cardOnOtherUserBoardId);
            verify(cardRepository, never()).delete(any(Card.class));
        }
    }

    @Nested
    @DisplayName("findById (internal helper) method tests")
    class FindByIdInternal {

        @Test
        @DisplayName("should return card when card exists")
        void shouldReturnCard_whenCardExists() {
            when(cardRepository.findById(cardOnCurrentUserBoardId)).thenReturn(Optional.of(cardOnCurrentUserBoard));
            
            Card foundCard = cardService.findById(cardOnCurrentUserBoardId);
            
            assertNotNull(foundCard);
            assertEquals(cardOnCurrentUserBoard, foundCard);
            verify(cardRepository, times(1)).findById(cardOnCurrentUserBoardId);
        }

        @Test
        @DisplayName("should throw NotFoundException when card does not exist")
        void shouldThrowNotFoundException_whenCardDoesNotExist() {
            
            UUID nonExistentCardId = UUID.randomUUID();
            when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

            
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> cardService.findById(nonExistentCardId));

            assertEquals("Card not found", exception.getMessage());
            verify(cardRepository, times(1)).findById(nonExistentCardId);
        }
    }
}