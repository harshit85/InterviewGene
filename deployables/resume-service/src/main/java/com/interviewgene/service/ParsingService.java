package com.interviewgene.service;

import com.interviewgene.model.ResumeData;
import org.springframework.stereotype.Service;

/**
 * Service interface for parsing resume documents
 */
@Service
public interface ParsingService {
    
    /**
     * Parse resume file and extract structured data
     * 
     * @param fileContent The file content as byte array
     * @param contentType The MIME type of the file
     * @return Parsed resume data
     */
    ResumeData parseResume(byte[] fileContent, String contentType);
}