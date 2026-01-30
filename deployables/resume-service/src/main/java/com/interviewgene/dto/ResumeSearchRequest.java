package com.interviewgene.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for resume search request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeSearchRequest {

    private List<String> skills;
    private Integer minExperience;
    private Integer maxExperience;
    private String jobTitle;
    private String company;
    private String degree;
    private String location;
    private Double minScore;
    private Double maxScore;
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
    
    @Builder.Default
    private String sortBy = "uploadedAt";
    
    @Builder.Default
    private String sortDirection = "desc";
}