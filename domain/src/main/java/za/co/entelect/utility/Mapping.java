package za.co.entelect.utility;

import za.co.entelect.dto.*;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.entity.CustomerEntity;
import za.co.entelect.entity.ReconciliationTransactionEntity;
import za.co.entelect.entity.TransactionEntity;

public class Mapping {

    public static Transaction toTransaction(TransactionEntity transactionEntity){
        return Transaction.builder()
//                .transactionID(transactionEntity.getTransactionID())
                .account(toTransactionAccount(transactionEntity.getAccount()))
                .amount(transactionEntity.getAmount())
                .transactionReference(transactionEntity.getTransactionReference())
                .transactionDescription(transactionEntity.getTransactionDescription())
                .transactionType(transactionEntity.getTransactionType())
                .transactionDate(transactionEntity.getTransactionDate())
                .counterpartyBankName(transactionEntity.getCounterpartyBankName())
                .processingBank(transactionEntity.getProcessingBank())
                .build();
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

    public static TransactionAccount toTransactionAccount(AccountEntity accountEntity){
        return TransactionAccount.builder()
                .accountID(accountEntity.getAccountID())
                .accountNumber(accountEntity.getAccountNumber())
                .accountType(accountEntity.getAccountType())
                .branchCode(accountEntity.getBranchCode())
                .build();
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

    public static CustomerEntity toCustomerEntityFromCustomer(Customer customer){
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setCustomerID(customer.getCustomerID());
        customerEntity.setName(customer.getName());
        customerEntity.setSurname(customer.getSurname());
        customerEntity.setIdentificationNumber(customer.getIdentificationNumber());
        customerEntity.setEmailAddress(customer.getEmailAddress());
        customerEntity.setCellphoneNumber(customer.getCellphoneNumber());
        return customerEntity;
    }

    public static CustomerEntity toCustomerEntityFromAccountCustomer(AccountCustomer customer){
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setCustomerID(customer.getCustomerID());
        customerEntity.setName(customer.getName());
        customerEntity.setSurname(customer.getSurname());
        customerEntity.setEmailAddress(customer.getEmailAddress());
        return customerEntity;
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

    public static AccountEntity toAccountEntity(Account account){
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setAccountID(account.getAccountID());
        accountEntity.setAccountNumber(account.getAccountNumber());
        accountEntity.setAccountType(account.getAccountType());
        accountEntity.setBranchCode(account.getBranchCode());
        accountEntity.setBalance(account.getBalance());
        accountEntity.setCustomer(toCustomerEntityFromAccountCustomer(account.getCustomer()));

        return accountEntity;
    }
}
