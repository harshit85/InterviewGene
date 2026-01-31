-- InterviewGene Database Schema Generation

CREATE DATABASE IF NOT EXISTS `access_db`;
USE `access_db`;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255),
  `email` VARCHAR(255) UNIQUE NOT NULL,
  `user_name` VARCHAR(255),
  `password` VARCHAR(255),
  `phone` VARCHAR(20),
  `role` VARCHAR(50) NOT NULL,
  `enabled` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--- 

CREATE DATABASE IF NOT EXISTS `user_db`;
USE `user_db`;

CREATE TABLE IF NOT EXISTS `user_profiles` (
  `user_id` CHAR(36) PRIMARY KEY,
  `external_id` VARCHAR(255) UNIQUE NOT NULL,
  `email` VARCHAR(255) UNIQUE NOT NULL,
  `first_name` VARCHAR(100),
  `last_name` VARCHAR(100),
  `phone_number` VARCHAR(20),
  `role` VARCHAR(50) NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT "PENDING",
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  `version` BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_activities` (
  `activity_id` CHAR(36) PRIMARY KEY,
  `user_id` CHAR(36) NOT NULL,
  `activity_type` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255),
  `metadata` TEXT,
  `ip_address` VARCHAR(45),
  `user_agent` VARCHAR(255),
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_preferences` (
  `user_id` CHAR(36) PRIMARY KEY,
  `language_preference` VARCHAR(10) DEFAULT "en",
  `timezone` VARCHAR(50) DEFAULT "UTC",
  `email_notifications` BOOLEAN DEFAULT TRUE,
  `push_notifications` BOOLEAN DEFAULT TRUE,
  `theme` VARCHAR(20) DEFAULT "light"
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_preference_metadata` (
  `user_id` CHAR(36),
  `meta_key` VARCHAR(100),
  `meta_value` VARCHAR(255),
  PRIMARY KEY (user_id, meta_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--- 

CREATE DATABASE IF NOT EXISTS `resume_db`;
USE `resume_db`;

CREATE TABLE IF NOT EXISTS `resumes` (
  `resume_id` CHAR(36) PRIMARY KEY,
  `candidate_id` CHAR(36) NOT NULL,
  `original_filename` VARCHAR(255) NOT NULL,
  `file_size` BIGINT,
  `content_type` VARCHAR(100),
  `s3_key` VARCHAR(255),
  `s3_bucket` VARCHAR(255),
  `status` VARCHAR(50) NOT NULL DEFAULT "UPLOADED",
  `uploaded_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `parsed_at` TIMESTAMP NULL,
  `last_analyzed_at` TIMESTAMP NULL,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  `created_by` CHAR(36),
  `updated_by` CHAR(36),
  `version` BIGINT,
  `is_active` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `work_experiences` (
  `experience_id` CHAR(36) PRIMARY KEY,
  `resume_id` CHAR(36) NOT NULL,
  `company_name` VARCHAR(255) NOT NULL,
  `job_title` VARCHAR(255) NOT NULL,
  `start_date` DATE,
  `end_date` DATE,
  `is_current` BOOLEAN DEFAULT FALSE,
  `location` VARCHAR(255),
  `description` TEXT,
  `employment_type` VARCHAR(50),
  `industry` VARCHAR(100),
  `company_size` VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `work_experience_responsibilities` (
  `experience_id` CHAR(36),
  `responsibility` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `work_experience_achievements` (
  `experience_id` CHAR(36),
  `achievement` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `work_experience_technologies` (
  `experience_id` CHAR(36),
  `technology` VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `educations` (
  `education_id` CHAR(36) PRIMARY KEY,
  `resume_id` CHAR(36) NOT NULL,
  `institution_name` VARCHAR(255) NOT NULL,
  `degree_type` VARCHAR(100),
  `field_of_study` VARCHAR(100),
  `major` VARCHAR(100),
  `minor` VARCHAR(100),
  `gpa` DOUBLE,
  `max_gpa` DOUBLE,
  `start_date` DATE,
  `end_date` DATE,
  `graduation_date` DATE,
  `location` VARCHAR(255),
  `description` TEXT,
  `is_completed` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `education_honors` (
  `education_id` CHAR(36),
  `honor` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `education_activities` (
  `education_id` CHAR(36),
  `activity` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `education_relevant_courses` (
  `education_id` CHAR(36),
  `course` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `resume_analyses` (
  `analysis_id` CHAR(36) PRIMARY KEY,
  `resume_id` CHAR(36) NOT NULL,
  `analysis_type` VARCHAR(100) NOT NULL,
  `overall_score` DOUBLE,
  `confidence_level` DOUBLE,
  `analysis_summary` TEXT,
  `strengths` TEXT,
  `weaknesses` TEXT,
  `recommendations` TEXT,
  `analyzed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `analyzed_by` CHAR(36),
  `ai_model_version` VARCHAR(50),
  `processing_time_ms` BIGINT,
  `version` BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `analysis_scores` (
  `analysis_id` CHAR(36),
  `score_category` VARCHAR(100),
  `score_value` DOUBLE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `analysis_metadata` (
  `analysis_id` CHAR(36),
  `metadata_key` VARCHAR(100),
  `metadata_value` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--- 

CREATE DATABASE IF NOT EXISTS `question_db`;
USE `question_db`;

CREATE TABLE IF NOT EXISTS `questions` (
  `question_id` CHAR(36) PRIMARY KEY,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `expected_answer` TEXT,
  `type` VARCHAR(50) NOT NULL,
  `difficulty` VARCHAR(50) NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT "ACTIVE",
  `category_id` CHAR(36),
  `estimated_time_minutes` INTEGER,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  `created_by` CHAR(36),
  `version` BIGINT,
  `is_active` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question_tags` (
  `question_id` CHAR(36),
  `tag` VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question_metadata` (
  `question_id` CHAR(36),
  `metadata_key` VARCHAR(100),
  `metadata_value` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question_categories` (
  `category_id` CHAR(36) PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `parent_id` CHAR(36),
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_active` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--- 

CREATE DATABASE IF NOT EXISTS `interview_db`;
USE `interview_db`;

CREATE TABLE IF NOT EXISTS `interview_sessions` (
  `session_id` CHAR(36) PRIMARY KEY,
  `interviewer_id` CHAR(36) NOT NULL,
  `candidate_id` CHAR(36) NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT "SCHEDULED",
  `scheduled_time` TIMESTAMP NULL,
  `start_time` TIMESTAMP NULL,
  `end_time` TIMESTAMP NULL,
  `session_type` VARCHAR(50) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` CHAR(36),
  `updated_by` CHAR(36),
  `version` BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `session_metadata` (
  `session_id` CHAR(36),
  `metadata_key` VARCHAR(100),
  `metadata_value` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `session_interactions` (
  `interaction_id` CHAR(36) PRIMARY KEY,
  `session_id` CHAR(36) NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `content` TEXT,
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `participant_id` CHAR(36) NOT NULL,
  `sequence_number` INTEGER,
  `duration_seconds` INTEGER,
  `created_by` CHAR(36),
  `version` BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `interaction_metadata` (
  `interaction_id` CHAR(36),
  `metadata_key` VARCHAR(100),
  `metadata_value` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--- 

CREATE DATABASE IF NOT EXISTS `evaluation_db`;
USE `evaluation_db`;

CREATE TABLE IF NOT EXISTS `evaluations` (
  `id` CHAR(36) PRIMARY KEY,
  `candidate_id` CHAR(36) NOT NULL,
  `resume_id` CHAR(36) NOT NULL,
  `job_id` CHAR(36) NOT NULL,
  `status` VARCHAR(50) DEFAULT "PENDING",
  `overall_score` DOUBLE,
  `summary` VARCHAR(2000),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `evaluation_results` (
  `id` CHAR(36) PRIMARY KEY,
  `evaluation_id` CHAR(36) NOT NULL,
  `criteria_id` CHAR(36) NOT NULL,
  `score` DOUBLE NOT NULL,
  `feedback` VARCHAR(1000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `evaluation_criteria` (
  `id` CHAR(36) PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(255),
  `type` VARCHAR(50) NOT NULL,
  `weight` INTEGER NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--- 

CREATE DATABASE IF NOT EXISTS `analytics_db`;
USE `analytics_db`;

CREATE TABLE IF NOT EXISTS `analytics_reports` (
  `report_id` CHAR(36) PRIMARY KEY,
  `title` VARCHAR(255) NOT NULL,
  `description` VARCHAR(1000),
  `status` VARCHAR(50) NOT NULL,
  `report_type` VARCHAR(50),
  `s3_export_key` VARCHAR(255),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `completed_at` TIMESTAMP NULL,
  `requested_by` CHAR(36)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `report_data` (
  `report_id` CHAR(36),
  `data_key` VARCHAR(100),
  `data_value` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `platform_metrics` (
  `metric_id` CHAR(36) PRIMARY KEY,
  `type` VARCHAR(50) NOT NULL,
  `value` DOUBLE NOT NULL,
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `service_origin` VARCHAR(100),
  `dimension_key` VARCHAR(100),
  `dimension_value` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--- 

