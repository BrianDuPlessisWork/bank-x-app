DROP TABLE IF EXISTS NotificationQueue;
DROP TABLE IF EXISTS Transaction;
DROP TABLE IF EXISTS Account;
DROP TABLE IF EXISTS CustomerAuthentication;
DROP TABLE IF EXISTS Customer;
DROP TABLE IF EXISTS ReconciliationTransaction;
DROP TABLE IF EXISTS ReconciliationBatch;

CREATE TABLE Customer (
    customerID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    surname VARCHAR(100),
    IdentificationNumber VARCHAR(50) UNIQUE,
    emailAddress VARCHAR(255),
    cellphoneNumber VARCHAR(20)
);

CREATE TABLE Account (
    accountID INT AUTO_INCREMENT PRIMARY KEY,
    accountNumber VARCHAR(50) UNIQUE,
    accountType VARCHAR(50),
    branchCode VARCHAR(20),
    balance DECIMAL(15,2),
    customerID INT,
    FOREIGN KEY (customerID) REFERENCES Customer(customerID) ON DELETE CASCADE
);

CREATE TABLE Transaction (
    transactionID INT AUTO_INCREMENT PRIMARY KEY,
    accountID INT,
    transactionReference VARCHAR(50),
    transactionType VARCHAR(50),
    amount DECIMAL(15,2),
    transactionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transactionDescription VARCHAR(50),
    processingBank VARCHAR(255),
    counterpartyBankName VARCHAR(255),
    FOREIGN KEY (accountID) REFERENCES Account(accountID) ON DELETE CASCADE
);

CREATE TABLE ReconciliationBatch (
    reconciliationBatchID INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(50),
    submissionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processingBank VARCHAR(255)
);

CREATE TABLE ReconciliationTransaction (
    reconciliationTransactionID INT AUTO_INCREMENT PRIMARY KEY,
    reconciliationBatchID INT,
    transactionID INT,
    accountNumber VARCHAR(50),
    accountType VARCHAR(50),
    branchCode VARCHAR(50),
    amount DECIMAL(15,2),
    transactionReference VARCHAR(50),
    transactionType VARCHAR(50),
    transactionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    counterpartyBankName VARCHAR(255),
    status VARCHAR(50),
    FOREIGN KEY (transactionID) REFERENCES Transaction(transactionID),
    FOREIGN KEY (reconciliationBatchID) REFERENCES ReconciliationBatch(reconciliationBatchID) ON DELETE CASCADE
);
