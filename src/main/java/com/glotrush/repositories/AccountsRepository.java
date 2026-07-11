package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.UserRole;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, UUID> {
    Optional<Accounts> findByEmail(String email);
    Optional<Accounts> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<Accounts> findByRole(UserRole role);

    @Query("SELECT DISTINCT up.account FROM UserProgress up WHERE up.topic.targetLanguage.id = :languageId AND up.account.id != :accountId AND up.account.status != com.glotrush.enumerations.AccountStatus.DELETED")
    List<Accounts> findAccountsByLanguageId (UUID languageId, UUID accountId);

    @Query("SELECT a FROM Accounts a WHERE a.status != com.glotrush.enumerations.AccountStatus.DELETED AND (LOWER(a.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Accounts> searchUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.role = :role")
    Long countTotalUsers(@Param("role") UserRole role);

    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.role = :role AND a.createdAt >= :since")
    Long countNewUsersSince(@Param("role") UserRole role, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.role = :role AND a.currentStreak > 0")
    Long countUsersWithActiveStreak(@Param("role") UserRole role);

    @Query("SELECT FUNCTION('TO_CHAR', a.createdAt, 'YYYY-MM') AS month, COUNT(a) FROM Accounts a WHERE a.role = :role AND a.createdAt >= :since GROUP BY FUNCTION('TO_CHAR', a.createdAt, 'YYYY-MM') ORDER BY month ASC")
    List<Object[]> countNewUsersByMonth(@Param("role") UserRole role, @Param("since") LocalDateTime since);

    @Query("SELECT a FROM Accounts a WHERE a.role = :role AND (:search IS NULL OR :search = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY a.username ASC")
    Page<Accounts> findUsersWithSearch(@Param("role") UserRole role, @Param("search") String search, Pageable pageable);
}
