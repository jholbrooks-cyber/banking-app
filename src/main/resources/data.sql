-- Passwords stored as MD5 (weak hashing, no salt)
-- admin123 = 0192023a7bbd73250516f069df18b500
-- password = 5f4dcc3b5aa765d61d8327deb882cf99
-- 123456   = e10adc3949ba59abbe56e057f20f883e

INSERT INTO users (username, password, email, full_name, role, phone, address) VALUES
('admin',      '0192023a7bbd73250516f069df18b500', 'admin@nbg.gr',      'System Administrator', 'ADMIN', '+30-210-0000001', 'Athens HQ, Syntagma Square'),
('john.doe',   '5f4dcc3b5aa765d61d8327deb882cf99', 'john.doe@email.com', 'John Doe',             'USER',  '+30-210-5551234', '14 Ermou Street, Athens'),
('jane.smith', 'e10adc3949ba59abbe56e057f20f883e', 'jane.smith@email.com','Jane Smith',          'USER',  '+30-210-5555678', '27 Voulis Street, Athens'),
('maria.k',    '5f4dcc3b5aa765d61d8327deb882cf99', 'maria.k@email.com',  'Maria Konstantinou',   'USER',  '+30-211-1112233', '3 Patission Avenue, Athens');

INSERT INTO accounts (account_number, user_id, account_type, balance, iban) VALUES
('NBG-0000001', 1, 'CHECKING', 999999.00, 'GR1601101250000000012300695'),
('NBG-0000002', 2, 'CHECKING', 12500.75,  'GR1601101250000000012300696'),
('NBG-0000003', 2, 'SAVINGS',  45000.00,  'GR1601101250000000012300697'),
('NBG-0000004', 3, 'CHECKING', 8300.20,   'GR1601101250000000012300698'),
('NBG-0000005', 4, 'SAVINGS',  23100.00,  'GR1601101250000000012300699');

INSERT INTO transactions (from_account, to_account, amount, description, transaction_type) VALUES
('NBG-0000002', 'NBG-0000004', 500.00,  'Rent payment',       'TRANSFER'),
('NBG-0000003', 'NBG-0000005', 1200.00, 'Invoice INV-2024-01','TRANSFER'),
('NBG-0000004', 'NBG-0000002', 200.00,  'Refund',             'TRANSFER'),
(NULL,          'NBG-0000002', 3000.00, 'Salary deposit',     'DEPOSIT'),
('NBG-0000003', NULL,          150.00,  'ATM withdrawal',     'WITHDRAWAL');
