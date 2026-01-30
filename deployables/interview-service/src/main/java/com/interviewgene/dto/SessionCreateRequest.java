package com.interviewgene.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for creating a new interview session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionCreateRequest {

    @NotNull(message = "Interviewer ID is required")
    private UUID interviewerId;

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    private LocalDateTime scheduledTime;

    @NotNull(message = "Session type is required")
    private String sessionType;

    private Map<String, String> metadata;
}