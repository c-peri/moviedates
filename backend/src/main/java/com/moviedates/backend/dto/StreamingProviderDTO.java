package com.moviedates.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamingProviderDTO {
    private String providerName;
    private String logoUrl;
}