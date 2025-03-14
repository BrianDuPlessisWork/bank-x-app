package za.co.entelect.utility;

import za.co.entelect.dto.*;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.entity.CustomerEntity;
import za.co.entelect.entity.ReconciliationTransactionEntity;
import za.co.entelect.entity.TransactionEntity;

public class Mapping {

    public static Transaction toTransaction(TransactionEntity transactionEntity) {
        Transaction transaction = new Transaction();

        transaction.setTransactionID(transactionEntity.getTransactionID());
        transaction.setAccount(toTransactionAccount(transactionEntity.getAccount()));
        transaction.setAmount(transactionEntity.getAmount());
        transaction.setTransactionReference(transactionEntity.getTransactionReference());
        transaction.setTransactionDescription(transactionEntity.getTransactionDescription());
        transaction.setTransactionType(transactionEntity.getTransactionType());
        transaction.setTransactionDate(transactionEntity.getTransactionDate());
        transaction.setCounterpartyBankName(transactionEntity.getCounterpartyBankName());
        transaction.setProcessingBank(transactionEntity.getProcessingBank());

        return transaction;
    }

    public static Account toAccount(AccountEntity accountEntity){
        return Account.builder()
                .accountID(accountEntity.getAccountID())
                .accountNumber(accountEntity.getAccountNumber())
                .accountType(accountEntity.getAccountType())
                .branchCode(accountEntity.getBranchCode())
                .balance(accountEntity.getBalance())
                .customer(toAccountCustomer(accountEntity.getCustomer()))
                .build();
    }

    public static TransactionAccount toTransactionAccount(AccountEntity accountEntity) {
        TransactionAccount transactionAccount = new TransactionAccount();
        transactionAccount.setAccountID(accountEntity.getAccountID());
        transactionAccount.setAccountNumber(accountEntity.getAccountNumber());
        transactionAccount.setAccountType(accountEntity.getAccountType());
        transactionAccount.setBranchCode(accountEntity.getBranchCode());

        return transactionAccount;
    }

    public static ReconciliationTransaction toReconciliationTransaction(ReconciliationTransactionEntity entity) {
        return ReconciliationTransaction.builder()
                .reconciliationTransactionID(entity.getReconciliationTransactionID())
                .accountNumber(entity.getAccountNumber())
                .accountType(entity.getAccountType())
                .branchCode(entity.getBranchCode())
                .amount(entity.getAmount())
                .transactionReference(entity.getTransactionReference())
                .transactionType(entity.getTransactionType())
                .transactionDate(entity.getTransactionDate())
                .counterpartyBankName(entity.getCounterpartyBankName())
                .status(entity.getStatus())
                .build();
    }

    public static AccountCustomer toAccountCustomer(CustomerEntity customerEntity){
        return AccountCustomer.builder()
                .customerID(customerEntity.getCustomerID())
                .name(customerEntity.getName())
                .surname(customerEntity.getSurname())
                .emailAddress(customerEntity.getEmailAddress())
                .build();
    }

    public static Customer toCustomer(CustomerEntity customerEntity){
        return Customer.builder()
                .customerID(customerEntity.getCustomerID())
                .name(customerEntity.getName())
                .surname(customerEntity.getSurname())
                .identificationNumber(customerEntity.getIdentificationNumber())
                .emailAddress(customerEntity.getEmailAddress())
                .cellphoneNumber(customerEntity.getCellphoneNumber())
                .build();
    }

}
