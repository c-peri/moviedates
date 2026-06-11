package com.moviedates.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String title;
    private String posterPath;
    private String overview;
    private Double voteAverage;
    private String releaseDate;
    private List<String> genres;
    private Integer runtime;
}