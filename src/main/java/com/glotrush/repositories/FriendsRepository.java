package com.glotrush.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Friends;
import com.glotrush.enumerations.FriendRequestStatus;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, UUID> {

    Optional<Friends> findBySender_IdAndReceiver_Id(UUID senderId, UUID receiverId);

    List<Friends> findByReceiver_IdAndStatus(UUID receiverId, FriendRequestStatus status);

    List<Friends> findBySender_IdAndStatus(UUID senderId, FriendRequestStatus status);

    @Query("SELECT f FROM Friends f WHERE (f.sender.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friends> findAcceptedRequests(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friends f WHERE ((f.sender.id = :firstUser AND f.receiver.id = :secondUser) OR (f.sender.id = :secondUser AND f.receiver.id = :firstUser)) AND f.status = 'ACCEPTED'")
    boolean isFriends(@Param("firstUser") UUID firstUserId, @Param("secondUser") UUID secondUserId);

    @Query("SELECT f FROM Friends f  WHERE (f.sender.id = :firstUser AND f.receiver.id = :secondUser) OR (f.sender.id = :secondUser AND f.receiver.id = :firstUser)")
    Optional<Friends> findBetweenTwoUsers(@Param("firstUser") UUID firstUserId, @Param("secondUser") UUID secondUserId);

    @Query("SELECT CASE WHEN f.sender.id = :userId THEN f.receiver.id ELSE f.sender.id END FROM Friends f WHERE (f.sender.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED'")
    List<UUID> findFriendIds(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM Friends f WHERE f.sender.id = :accountId OR f.receiver.id = :accountId")
    void deleteAllByAccountId(@Param("accountId") UUID accountId);
}

