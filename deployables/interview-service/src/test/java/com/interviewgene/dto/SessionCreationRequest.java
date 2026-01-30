package com.interviewgene.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for session creation requests used in property-based testing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionCreationRequest {
    private UUID interviewerId;
    private UUID candidateId;
    private LocalDateTime scheduledTime;
    private String sessionType;
    private Map<String, String> metadata;
}