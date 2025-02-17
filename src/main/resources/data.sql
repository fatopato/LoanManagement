-- Insert a test customer user
INSERT INTO users (
    username, password, email, full_name, role, active,
    name, surname, credit_limit, used_credit_limit
)
VALUES (
    'customer1',
    -- password is 'customer123' encoded with BCrypt
    '$2a$10$vCXMWCn7fDZWOcLnIEhmK.74dvK1Eh8ae2WrWlhr2ETPLoxQctXWG',
    'customer1@example.com',
    'John Doe',
    'ROLE_CUSTOMER',
    true,
    'John',
    'Doe',
    50000.00,
    0.00
);

-- Insert sample loan for testing
INSERT INTO loans (user_id, loan_amount, number_of_installments, create_date, is_paid)
VALUES 
    (1, 10000.00, 12, CURRENT_DATE, false);

-- Insert sample loan installments
INSERT INTO loan_installments (loan_id, amount, due_date, is_paid)
VALUES 
    (1, 1000.00, DATEADD('MONTH', 1, CURRENT_DATE), false),
    (1, 1000.00, DATEADD('MONTH', 2, CURRENT_DATE), false),
    (1, 1000.00, DATEADD('MONTH', 3, CURRENT_DATE), false); 