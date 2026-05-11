package com.moviedates.backend.repository;

import com.moviedates.backend.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    // 1. Keep your existing count check for specific logic
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.session.id = :sessionId AND v.movieId = :movieId AND v.accepted = true")
    long countAcceptances(@Param("sessionId") Long sessionId, @Param("movieId") Integer movieId);

    // 2. The "Match Finder": Returns true if the count of acceptances
    // matches the total number of users in that session.
    @Query("SELECT (COUNT(v) = :userCount) FROM Vote v " +
            "WHERE v.session.id = :sessionId " +
            "AND v.movieId = :movieId " +
            "AND v.accepted = true")
    boolean isMovieAMatch(@Param("sessionId") Long sessionId,
                          @Param("movieId") Integer movieId,
                          @Param("userCount") long userCount);

    // 3. Optional: Get a list of all movies that have been matched in a session
    @Query("SELECT v.movieId FROM Vote v " +
            "WHERE v.session.id = :sessionId AND v.accepted = true " +
            "GROUP BY v.movieId " +
            "HAVING COUNT(v.user.id) = :userCount")
    List<Integer> findAllMatchedMovieIds(@Param("sessionId") Long sessionId,
                                         @Param("userCount") long userCount);

    @Query(value = "SELECT v.movie_id FROM votes v " +
            "WHERE v.session_id = :sessionId AND v.accepted = true " +
            "GROUP BY v.movie_id " +
            "ORDER BY COUNT(v.user_id) DESC " +
            "LIMIT 1", nativeQuery = true)
    Integer findMostVotedMovie(@Param("sessionId") Long sessionId);
}