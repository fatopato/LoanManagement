-- Drop tables in correct order to avoid foreign key constraints
DROP TABLE IF EXISTS loan_installments;
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS users;

-- Create users table with role as enum and customer fields
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    name VARCHAR(255),
    surname VARCHAR(255),
    credit_limit DECIMAL(19,2),
    used_credit_limit DECIMAL(19,2) DEFAULT 0,
    CONSTRAINT chk_role CHECK (role IN ('ROLE_ADMIN', 'ROLE_CUSTOMER'))
);

-- Create loans table
CREATE TABLE loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    loan_amount DECIMAL(19,2) NOT NULL,
    number_of_installments INT NOT NULL,
    create_date DATE NOT NULL,
    is_paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create loan_installments table
CREATE TABLE loan_installments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    paid_amount DECIMAL(19,2) DEFAULT 0,
    due_date DATE NOT NULL,
    payment_date DATE,
    is_paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (loan_id) REFERENCES loans(id)
); 