package com.interviewgene.model;

/**
 * Enumeration for resume processing status
 */
public enum ResumeStatus {
    /**
     * Resume has been uploaded but not yet processed
     */
    UPLOADED,
    
    /**
     * Resume is currently being parsed
     */
    PARSING,
    
    /**
     * Resume has been successfully parsed
     */
    PARSED,
    
    /**
     * Resume is being analyzed by AI
     */
    ANALYZING,
    
    /**
     * Resume analysis is complete
     */
    ANALYZED,
    
    /**
     * Resume parsing failed
     */
    PARSE_FAILED,
    
    /**
     * Resume analysis failed
     */
    ANALYSIS_FAILED,
    
    /**
     * Resume has been archived
     */
    ARCHIVED,
    
    /**
     * Resume has been deleted
     */
    DELETED
}