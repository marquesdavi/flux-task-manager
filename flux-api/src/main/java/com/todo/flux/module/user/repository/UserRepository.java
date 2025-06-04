package com.todo.flux.module.user.repository;

import com.todo.flux.module.user.dto.UserSummary;
import com.todo.flux.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT new com.todo.flux.module.user.dto.UserSummary(u.id, u.firstName, u.lastName, u.email) " +
            "FROM Board b " +
            "JOIN b.collaborators u " +
            "WHERE b.id = :boardId")
    List<UserSummary> findDistinctByBoardId(@Param("boardId") UUID boardId);
}
