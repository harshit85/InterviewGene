package com.interviewgene.dto;

import com.interviewgene.model.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for updating an existing interview session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionUpdateRequest {

    private SessionStatus status;
    private LocalDateTime scheduledTime;
    private String sessionType;
    private Map<String, String> metadata;
}