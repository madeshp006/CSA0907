-- ==============================================================================
-- SIMATS ENGINEERING - DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING
-- COURSE OUTCOME 4 (CO4): EVENT-DRIVEN GUI DESIGN (CSA09)
-- TASK 6: CAMPUS EVENT REGISTRATION DATABASE SCHEMA & QUERIES
-- 
-- Student: P. Madesh (Register No: 192365007)
-- Faculty Evaluator: Surya V
-- ==============================================================================

-- 1. Database Creation
CREATE DATABASE IF NOT EXISTS campus_events;
USE campus_events;

-- 2. Clean Existing Schema if Needed
DROP TABLE IF EXISTS event_registrations;
DROP TABLE IF EXISTS events_master;

-- 3. Master Events Table
CREATE TABLE events_master (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    event_name VARCHAR(100) NOT NULL UNIQUE,
    event_date DATE NOT NULL,
    venue VARCHAR(100) NOT NULL,
    max_capacity INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed Events
INSERT INTO events_master (event_name, event_date, venue, max_capacity) VALUES
('Hackathon 2026', '2026-12-10', 'Tech Park Lab 4', 150),
('AI CodeSprint', '2026-12-11', 'Turing Auditorium', 100),
('RoboWars Championship', '2026-12-12', 'Mechanical Arena', 80),
('Web Dev Showcase', '2026-12-13', 'Computing Center 2', 120),
('Algorithmic Duel', '2026-12-14', 'Seminar Hall Alpha', 75);

-- 4. Event Registrations Table (Core Requirement)
CREATE TABLE event_registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    reg_no VARCHAR(20) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    event_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    
    -- BUSINESS RULE INVARIANT: Prevent duplicate registration for the same event
    CONSTRAINT uq_student_event UNIQUE (reg_no, event_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Seed Realistic Benchmark Registrations
INSERT INTO event_registrations (reg_no, student_name, department, event_name, email, phone, status) VALUES
('192365007', 'P. Madesh', 'Computer Science & Engg', 'Hackathon 2026', 'madesh.p@saveetha.ac.in', '9876543210', 'CONFIRMED'),
('192365018', 'R. Vignesh', 'Computer Science & Engg', 'AI CodeSprint', 'vignesh.r@saveetha.ac.in', '9876543211', 'CONFIRMED'),
('192365022', 'S. Priya', 'Information Technology', 'Hackathon 2026', 'priya.s@saveetha.ac.in', '9876543212', 'CONFIRMED'),
('192365035', 'K. Arjun', 'Electronics & Comm Engg', 'RoboWars Championship', 'arjun.k@saveetha.ac.in', '9876543213', 'CONFIRMED'),
('192365041', 'M. Divya', 'Computer Science & Engg', 'Web Dev Showcase', 'divya.m@saveetha.ac.in', '9876543214', 'CONFIRMED'),
('192365050', 'T. Naveen', 'Mechanical Engineering', 'RoboWars Championship', 'naveen.t@saveetha.ac.in', '9876543215', 'CONFIRMED'),
('192365063', 'A. Swetha', 'Bio-Technology', 'AI CodeSprint', 'swetha.a@saveetha.ac.in', '9876543216', 'CONFIRMED'),
('192365077', 'V. Karthik', 'Computer Science & Engg', 'Algorithmic Duel', 'karthik.v@saveetha.ac.in', '9876543217', 'CONFIRMED');

-- 6. Essential Operational Queries

-- Query 1: Retrieve All Registrations Order by Date
SELECT registration_id, reg_no, student_name, department, event_name, email, phone, registered_at, status
FROM event_registrations
ORDER BY registration_id DESC;

-- Query 2: Filter Registrations by Event with Pagination (LIMIT & OFFSET for scalability)
SELECT registration_id, reg_no, student_name, department, event_name, email, registered_at
FROM event_registrations
WHERE event_name = 'Hackathon 2026'
ORDER BY registration_id ASC
LIMIT 50 OFFSET 0;

-- Query 3: Duplicate Registration Guard Check
SELECT COUNT(*) AS is_duplicate
FROM event_registrations
WHERE reg_no = '192365007' AND event_name = 'Hackathon 2026';

-- Query 4: Aggregate Department-wise and Event-wise Metrics
SELECT event_name, COUNT(*) AS total_registrations
FROM event_registrations
GROUP BY event_name
ORDER BY total_registrations DESC;
