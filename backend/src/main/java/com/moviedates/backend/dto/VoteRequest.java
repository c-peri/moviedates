package com.moviedates.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {
    private Long userId;
    private Long sessionId;
    private Integer movieId;
    private boolean accepted;
}