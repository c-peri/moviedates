package com.moviedates.backend.repository;

import com.moviedates.backend.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    // Cleaner name using a JPQL query
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.session.id = :sessionId AND v.movieId = :movieId AND v.accepted = true")
    long countAcceptances(@Param("sessionId") Long sessionId, @Param("movieId") Integer movieId);
}