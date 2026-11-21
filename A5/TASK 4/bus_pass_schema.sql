-- ==============================================================================
-- SIMATS ENGINEERING - DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING
-- COURSE OUTCOME 5 (CO5): DATA PERSISTENCE ENGINEERING (CSA09)
-- TASK B3 & B4: BUS PASS DATABASE SCHEMA & BATCH PROCESSING SCRIPTS
-- 
-- Student: P. Madesh (Register No: 192365007)
-- Faculty Evaluator: Surya V
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS campus_transport_db;
USE campus_transport_db;

-- 1. Route Master Table
DROP TABLE IF EXISTS route_master;
CREATE TABLE route_master (
    route_no INT PRIMARY KEY,
    route_name VARCHAR(100) NOT NULL,
    start_point VARCHAR(100) NOT NULL,
    end_point VARCHAR(100) NOT NULL,
    bus_count INT NOT NULL DEFAULT 2
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO route_master (route_no, route_name, start_point, end_point, bus_count) VALUES
(7, 'Tambaram Express', 'Tambaram', 'Saveetha Campus', 4),
(12, 'Avadi Central', 'Avadi', 'Saveetha Campus', 3),
(14, 'Velachery Direct', 'Velachery', 'Saveetha Campus', 5),
(20, 'Kanchipuram Fast', 'Kanchipuram', 'Saveetha Campus', 2);

-- 2. Bus Pass Master Table (Task B3 Core Table)
DROP TABLE IF EXISTS bus_pass;
CREATE TABLE bus_pass (
    pass_id VARCHAR(20) PRIMARY KEY,
    reg_no VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    route_no INT NOT NULL,
    amount DOUBLE NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    CONSTRAINT fk_bus_route FOREIGN KEY (route_no) REFERENCES route_master(route_no)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Optimization Indexes for 12-Campus Scale (800,000 passes)
CREATE INDEX idx_buspass_regno ON bus_pass(reg_no);
CREATE INDEX idx_buspass_route ON bus_pass(route_no, status);

-- 4. Sample Seed Records for Verification
INSERT INTO bus_pass (pass_id, reg_no, name, route_no, amount) VALUES
('P1001', '192365007', 'P. Madesh', 14, 4200.0),
('P1002', '192365018', 'R. Vignesh', 14, 4200.0),
('P1003', '192365022', 'S. Priya', 7, 3600.0),
('P1004', '192365035', 'K. Arjun', 14, 4200.0),
('P1005', '192365041', 'M. Divya', 12, 4500.0);

-- 5. Wallet Accounts Table for Task A5.1 Transaction Verification
DROP TABLE IF EXISTS wallet_accounts;
CREATE TABLE wallet_accounts (
    account_id INT PRIMARY KEY,
    account_name VARCHAR(100) NOT NULL,
    balance DOUBLE NOT NULL CHECK (balance >= 0),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO wallet_accounts (account_id, account_name, balance) VALUES
(101, 'Hostel Wallet (Madesh)', 2500.0),
(202, 'Campus Canteen Terminal', 10000.0);

-- 6. Verification Queries
SELECT pass_id, reg_no, name, route_no, amount, status FROM bus_pass WHERE route_no = 14;
SELECT * FROM wallet_accounts;
