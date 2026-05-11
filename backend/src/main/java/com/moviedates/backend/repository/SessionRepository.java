package com.moviedates.backend.repository;

import com.moviedates.backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByCode(String code);

    @Query("SELECT s FROM Session s WHERE s.swipingStartedAt < :cutoff AND s.finished = false")
    List<Session> findTimedOutSessions(@Param("cutoff") LocalDateTime cutoff);}