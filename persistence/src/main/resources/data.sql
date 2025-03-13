INSERT INTO Customer (name, surname, IdentificationNumber, emailAddress, cellphoneNumber)
VALUES ('John', 'Smith', 'ID12345678', 'john.smith@email.com', '+27731234567'),
       ('Sarah', 'Johnson', 'ID87654321', 'sarah.johnson@email.com', '+27821234567');

INSERT INTO Account (accountNumber, accountType, branchCode, balance, customerID)
VALUES ('4325653324', 'SAVINGS', '051001', 5000.75, 1),
       ('7654536577', 'CURRENT', '051002', 12500.25, 2);

--INSERT INTO Transaction (accountID, transactionReference, transactionType, amount, transactionDate, transactionDescription, processingBank, counterpartyBankName)
--VALUES (1, 'ACC007654321', 'Deposit', 1500.00, '2025-03-10 09:15:00', 'Salary deposit', 'FirstBank', 'N/A'),
--       (2, 'ACC001234567', 'Withdrawal', 500.50, '2025-03-11 14:30:00', 'ATM withdrawal', 'FirstBank', 'N/A');
--
--INSERT INTO ReconciliationBatch (status, submissionDate, processingBank)
--VALUES ('Completed', '2025-03-01 00:01:00', 'FirstBank'),
--       ('Pending', '2025-03-12 00:01:00', 'SecondBank');
--
--INSERT INTO ReconciliationTransaction (reconciliationBatchID, accountNumber, accountType, branchCode, amount, transactionReference, transactionType, transactionDate, counterpartyBankName, status)
--VALUES (1, 'ACC001234567', 'Savings', '051001', 1500.00, 'ACC007654321', 'Deposit', '2025-03-10 09:15:00', 'N/A', 'Matched'),
--       (2, 'ACC007654321', 'Current', '051002', 500.50, 'ACC001234567', 'Withdrawal', '2025-03-11 14:30:00', 'N/A', 'Unmatched');