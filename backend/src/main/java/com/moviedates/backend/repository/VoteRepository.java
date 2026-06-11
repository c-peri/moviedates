package com.moviedates.backend.repository;

import com.moviedates.backend.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.session.id = :sessionId AND v.movieId = :movieId AND v.accepted = true")
    long countAcceptances(@Param("sessionId") Long sessionId, @Param("movieId") Integer movieId);

    @Query("SELECT (COUNT(v) = :userCount) FROM Vote v " +
            "WHERE v.session.id = :sessionId " +
            "AND v.movieId = :movieId " +
            "AND v.accepted = true")
    boolean isMovieAMatch(@Param("sessionId") Long sessionId,
                          @Param("movieId") Integer movieId,
                          @Param("userCount") long userCount);

    // future feature possibly :P
    @Query("SELECT v.movieId FROM Vote v " +
            "WHERE v.session.id = :sessionId AND v.accepted = true " +
            "GROUP BY v.movieId " +
            "HAVING COUNT(v.user.id) = :userCount")
    List<Integer> findAllMatchedMovieIds(@Param("sessionId") Long sessionId,
                                         @Param("userCount") long userCount);

    @Query(value = "SELECT v.movie_id FROM vote v " +
            "WHERE v.session_id = :sessionId AND v.accepted = true " +
            "GROUP BY v.movie_id " +
            "ORDER BY COUNT(v.id) DESC, MIN(v.timestamp) ASC " +
            "LIMIT 1", nativeQuery = true)
    Integer findMostVotedMovie(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(DISTINCT v.user.id) FROM Vote v " +
            "WHERE v.session.id = :sessionId " +
            "GROUP BY v.user.id " +
            "HAVING COUNT(v.id) >= :minPerUser")
    Long countUsersMinimumSwipes(@Param("sessionId") Long sessionId,
                                     @Param("minPerUser") Long minPerUser);

    @Query("SELECT COUNT(v) > 0 FROM Vote v WHERE v.session.id = :sessionId AND v.user.id = :userId AND v.movieId = :movieId")
    boolean existsBySessionIdAndUserIdAndMovieId(@Param("sessionId") Long sessionId, @Param("userId") Long userId, @Param("movieId") Integer movieId);
}