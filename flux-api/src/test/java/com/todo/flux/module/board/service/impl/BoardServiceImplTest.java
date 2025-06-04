package com.todo.flux.module.board.service.impl;

import com.todo.flux.exception.NotFoundException;
import com.todo.flux.module.auth.dto.LoginRequest;
import com.todo.flux.module.auth.dto.TokenResponse;
import com.todo.flux.module.auth.service.AuthenticationService;
import com.todo.flux.module.board.dto.BoardCreateRequest;
import com.todo.flux.module.board.dto.BoardResponse;
import com.todo.flux.module.board.dto.BoardUpdateRequest;
import com.todo.flux.module.board.entity.Board;
import com.todo.flux.module.board.repository.BoardRepository;
import com.todo.flux.module.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardServiceImpl Tests")
class BoardServiceImplTest {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private AuthenticationService<User, LoginRequest, TokenResponse> authService;
    @InjectMocks
    private BoardServiceImpl boardService;

    private User testUser;
    private Long testUserId;
    private UUID boardId;

    @BeforeEach
    void setUp() {
        testUserId = RandomGenerator.getDefault().nextLong();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
        boardId = UUID.randomUUID();
    }

    private Board createTestBoard(UUID id, String title, User owner) {
        return Board.builder()
                .id(id)
                .title(title)
                .owner(owner)
                .build();
    }

    private BoardResponse createTestBoardResponse(UUID id, String title, Long ownerId) {
        return new BoardResponse(id, title, ownerId,null, null, null);
    }


    @Nested
    @DisplayName("create Method Tests")
    class CreateBoard {

        @Test
        @DisplayName("Should create board successfully")
        void create_shouldCreateBoardSuccessfully() {
            BoardCreateRequest request = new BoardCreateRequest("New Board");
            BoardResponse expectedResponse = createTestBoardResponse(boardId, request.title(), testUser.getId());

            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
                Board boardBeingSaved = invocation.getArgument(0);
                return createTestBoard(boardId, boardBeingSaved.getTitle(), boardBeingSaved.getOwner());
            });

            BoardResponse actualResponse = boardService.create(request);

            assertNotNull(actualResponse);
            assertEquals(expectedResponse.id(), actualResponse.id());
            assertEquals(expectedResponse.title(), actualResponse.title());
            assertEquals(expectedResponse.ownerId(), actualResponse.ownerId());

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).save(argThat(board ->
                    board.getTitle().equals(request.title()) &&
                            board.getOwner().equals(testUser)
            ));
        }
    }

    @Nested
    @DisplayName("listAllForUser Method Tests")
    class ListAllForUser {

        @Test
        @DisplayName("Should return list of boards for user")
        void listAllForUser_shouldReturnListOfBoards() {
            Board board1 = createTestBoard(UUID.randomUUID(), "Board 1", testUser);
            Board board2 = createTestBoard(UUID.randomUUID(), "Board 2", testUser);
            List<Board> boards = List.of(board1, board2);

            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findByOwnerId(testUserId)).thenReturn(boards);

            List<BoardResponse> expectedResponses = boards.stream()
                    .map(b -> createTestBoardResponse(b.getId(), b.getTitle(), b.getOwner().getId()))
                    .toList();

            List<BoardResponse> actualResponses = boardService.listAllForUser();

            assertNotNull(actualResponses);
            assertEquals(2, actualResponses.size());
            assertEquals(expectedResponses.getFirst().id(), actualResponses.getFirst().id());
            assertEquals(expectedResponses.get(0).title(), actualResponses.get(0).title());
            assertEquals(expectedResponses.get(0).ownerId(), actualResponses.get(0).ownerId());

            assertEquals(expectedResponses.get(1).id(), actualResponses.get(1).id());
            assertEquals(expectedResponses.get(1).title(), actualResponses.get(1).title());
            assertEquals(expectedResponses.get(1).ownerId(), actualResponses.get(1).ownerId());


            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findByOwnerId(testUserId);
        }

        @Test
        @DisplayName("Should return empty list if user has no boards")
        void listAllForUser_shouldReturnEmptyListForNoBoards() {
            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findByOwnerId(testUserId)).thenReturn(Collections.emptyList());

            List<BoardResponse> actualResponses = boardService.listAllForUser();

            assertNotNull(actualResponses);
            assertTrue(actualResponses.isEmpty());

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findByOwnerId(testUserId);
        }
    }

    @Nested
    @DisplayName("getById Method Tests")
    class GetById {

        @Test
        @DisplayName("Should return board if found and owned by user")
        void getById_shouldReturnBoard_whenFoundAndOwnedByUser() {
            Board board = createTestBoard(boardId, "Test Board", testUser);
            BoardResponse expectedResponse = createTestBoardResponse(boardId, "Test Board", testUser.getId());

            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

            BoardResponse actualResponse = boardService.getById(boardId);

            assertNotNull(actualResponse);
            assertEquals(expectedResponse.id(), actualResponse.id());
            assertEquals(expectedResponse.title(), actualResponse.title());
            assertEquals(expectedResponse.ownerId(), actualResponse.ownerId());

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findById(boardId);
        }

        @Test
        @DisplayName("Should throw NotFoundException if board not found")
        void getById_shouldThrowNotFoundException_whenBoardNotFound() {
            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    boardService.getById(boardId)
            );
            assertEquals("Board not found", exception.getMessage());

            verify(authService, times(1)).getAuthenticated(); // Called before findById in service
            verify(boardRepository, times(1)).findById(boardId);
        }

        @Test
        @DisplayName("Should throw NotFoundException if board not owned by user")
        void getById_shouldThrowNotFoundException_whenBoardNotOwnedByUser() {
            Long anotherUserId = RandomGenerator.getDefault().nextLong();
            User anotherUserWithLongId = User.builder().id(anotherUserId).build();

            Board board = createTestBoard(boardId, "Test Board", anotherUserWithLongId);

            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    boardService.getById(boardId)
            );
            assertEquals("Board not found for this user", exception.getMessage());

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findById(boardId);
        }
    }

    @Nested
    @DisplayName("update Method Tests")
    class UpdateBoard {
        private BoardUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new BoardUpdateRequest("Updated Title");
        }

        @Test
        @DisplayName("Should update board successfully")
        void update_shouldUpdateBoardSuccessfully() {
            Board existingBoard = createTestBoard(boardId, "Old Title", testUser);
            BoardResponse expectedResponse = createTestBoardResponse(boardId, updateRequest.title(), testUser.getId());

            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(existingBoard));
            when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
                Board boardBeingSaved = invocation.getArgument(0);
                assertEquals(updateRequest.title(), boardBeingSaved.getTitle());
                return boardBeingSaved;
            });

            BoardResponse actualResponse = boardService.update(boardId, updateRequest);

            assertNotNull(actualResponse);
            assertEquals(expectedResponse.id(), actualResponse.id());
            assertEquals(expectedResponse.title(), actualResponse.title());
            assertEquals(expectedResponse.ownerId(), actualResponse.ownerId());

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findById(boardId);
            verify(boardRepository, times(1)).save(existingBoard);
            assertEquals(updateRequest.title(), existingBoard.getTitle());
        }

        @Test
        @DisplayName("Should throw NotFoundException if board to update not found")
        void update_shouldThrowNotFoundException_whenBoardNotFound() {
            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    boardService.update(boardId, updateRequest)
            );
            assertEquals("Board not found", exception.getMessage());

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findById(boardId);
            verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException if board to update not owned by user")
        void update_shouldThrowNotFoundException_whenBoardNotOwnedByUser() {
            Long anotherUserId = RandomGenerator.getDefault().nextLong();
            User anotherUser = User.builder().id(anotherUserId).build();
            Board existingBoard = createTestBoard(boardId, "Old Title", anotherUser);

            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(existingBoard));

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    boardService.update(boardId, updateRequest)
            );
            assertEquals("Board not found for this user", exception.getMessage());

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findById(boardId);
            verify(boardRepository, never()).save(any(Board.class));
        }
    }

    @Nested
    @DisplayName("delete Method Tests")
    class DeleteBoard {

        @Test
        @DisplayName("Should delete board successfully")
        void delete_shouldDeleteBoardSuccessfully() {
            Board boardToDelete = createTestBoard(boardId, "Test Board", testUser);

            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(boardToDelete));
            doNothing().when(boardRepository).delete(boardToDelete);

            assertDoesNotThrow(() -> boardService.delete(boardId));

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findById(boardId);
            verify(boardRepository, times(1)).delete(boardToDelete);
        }

        @Test
        @DisplayName("Should throw NotFoundException if board to delete not found")
        void delete_shouldThrowNotFoundException_whenBoardNotFound() {
            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    boardService.delete(boardId)
            );
            assertEquals("Board not found", exception.getMessage());

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findById(boardId);
            verify(boardRepository, never()).delete(any(Board.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException if board to delete not owned by user")
        void delete_shouldThrowNotFoundException_whenBoardNotOwnedByUser() {
            Long anotherUserId = RandomGenerator.getDefault().nextLong();
            User anotherUser = User.builder().id(anotherUserId).build();
            Board boardToDelete = createTestBoard(boardId, "Test Board", anotherUser);

            when(authService.getAuthenticated()).thenReturn(testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(boardToDelete));

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    boardService.delete(boardId)
            );
            assertEquals("Board not found for this user", exception.getMessage());

            verify(authService, times(1)).getAuthenticated();
            verify(boardRepository, times(1)).findById(boardId);
            verify(boardRepository, never()).delete(any(Board.class));
        }
    }

    @Nested
    @DisplayName("findById (public helper) Method Tests")
    class FindByIdPublicHelper {

        @Test
        @DisplayName("Should return board if found by repository")
        void findById_shouldReturnBoard_whenFound() {
            Board expectedBoard = createTestBoard(boardId, "Test Board", testUser);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(expectedBoard));

            Board actualBoard = boardService.findById(boardId);

            assertNotNull(actualBoard);
            assertEquals(expectedBoard.getId(), actualBoard.getId());
            assertEquals(expectedBoard.getTitle(), actualBoard.getTitle());
            assertEquals(testUser.getId(), actualBoard.getOwner().getId());

            verify(boardRepository, times(1)).findById(boardId);
        }

        @Test
        @DisplayName("Should throw NotFoundException if board not found by repository")
        void findById_shouldThrowNotFoundException_whenNotFound() {
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    boardService.findById(boardId)
            );
            assertEquals("Board not found", exception.getMessage());

            verify(boardRepository, times(1)).findById(boardId);
        }
    }
}