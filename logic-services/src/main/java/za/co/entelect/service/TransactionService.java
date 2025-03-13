package za.co.entelect.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import za.co.entelect.AccountRepository;
import za.co.entelect.CustomerRepository;
import za.co.entelect.TransactionRepository;
import za.co.entelect.dto.Account;
import za.co.entelect.dto.Customer;
import za.co.entelect.dto.Transaction;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.entity.CustomerEntity;
import za.co.entelect.entity.TransactionEntity;
import za.co.entelect.utility.Mapping;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final NotificationService notificationService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.notificationService = notificationService;
    }

    public List<Transaction> getTransactionsByAccountNumber(String accountNumber, Customer customer) throws AccessDeniedException {
        Account account = accountService.findAccountByAccountNumber(accountNumber);

        if (!account.getCustomer().getCustomerID().equals(customer.getCustomerID())) {
            throw new AccessDeniedException("The customer does not own the specified account");
        }

        List<TransactionEntity> transactionEntityList = transactionRepository.findByAccountNumber(accountNumber);

        return transactionEntityList.stream().map(Mapping::toTransaction).toList();
    }

    public Account createSignUpBonusTransaction(AccountEntity account){
        final BigDecimal joiningBonus = BigDecimal.valueOf(500.00);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setTransactionType("CREDIT");
        transaction.setAmount(joiningBonus);
        transaction.setTransactionDescription("JOINING BONUS");
        transaction.setTransactionReference(account.getAccountNumber());
        transaction.setTransactionDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        transaction.setProcessingBank("BANK_X");
        transaction.setCounterpartyBankName("BANK_X");

        account = addTransactionToAccount(account, transaction);
        account.setBalance(account.getBalance().add(transaction.getAmount()));

        Account returnAccount = accountService.updateAccount(account);
        notificationService.NotifyAccountHolder("Joining bonus of R " + transaction.getAmount() + " has been credited to to your account with account number: " + account.getAccountNumber());

        return returnAccount;
    }

    public AccountEntity createTransactionFeesTransaction(AccountEntity account, BigDecimal amount){
        final BigDecimal transactionFee = calculateTransactionFee(amount);

        if(transactionFee.compareTo(account.getBalance()) > 0){
            throw new IllegalArgumentException("Insufficient funds to make transfer");
        }

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setTransactionType("DEBIT");
        transaction.setAmount(transactionFee);
        transaction.setTransactionDescription("TRANSACTION FEES");
        transaction.setTransactionReference(account.getAccountNumber());
        transaction.setTransactionDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        transaction.setProcessingBank("BANK_X");
        transaction.setCounterpartyBankName("BANK_X");

        account = addTransactionToAccount(account, transaction);
        account.setBalance(account.getBalance().subtract(transaction.getAmount()));

        return account;
    }

    public AccountEntity createSavingsInterestTransaction(AccountEntity account){
        final BigDecimal interestPercentage = BigDecimal.valueOf(0.5);
        final BigDecimal interestAmount = (account.getBalance()).multiply(interestPercentage);

        if((account.getBalance()).compareTo(BigDecimal.ZERO) < 1){
            return account;
        }

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setTransactionType("CREDIT");
        transaction.setAmount(interestAmount);
        transaction.setTransactionDescription("SAVINGS INTEREST");
        transaction.setTransactionReference(account.getAccountNumber());
        transaction.setTransactionDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        transaction.setProcessingBank("BANK_X");
        transaction.setCounterpartyBankName("BANK_X");

        account = addTransactionToAccount(account, transaction);
        account.setBalance(account.getBalance().add(transaction.getAmount()));

        return account;
    }

    public AccountEntity addTransactionToAccount(AccountEntity account, TransactionEntity transaction){

        if(account.getTransactions() == null){
            account.setTransactions(new ArrayList<>());
        }

        List<TransactionEntity> accountTransactions = account.getTransactions();
        accountTransactions.add(transaction);
        account.setTransactions(accountTransactions);
        return account;
    }

    private BigDecimal calculateTransactionFee(BigDecimal amount){
        final BigDecimal transactionFeePercentage = BigDecimal.valueOf(0.05);
        return amount.multiply(transactionFeePercentage);
    }

    public Transaction debitAccount(String accountNumber, Customer customer, BigDecimal amount, String processingBank, String counterpartyBank, String transactionReference, String transactionDescription) throws AccessDeniedException {
        AccountEntity account = accountService.findAccountEntityByAccountNumber(accountNumber);

        if (!account.getCustomer().getCustomerID().equals(customer.getCustomerID())) {
            throw new AccessDeniedException("The customer does not own the specified account");
        }

        if ((account.getBalance().add(calculateTransactionFee(amount))).compareTo(amount) < 0) {
            throw new IllegalArgumentException("Customer does not have sufficient balance for the transfer");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Invalid amount supplied for debit transaction, please provide a valid amount");
        }

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setTransactionType("DEBIT");
        transaction.setAmount(amount);
        transaction.setTransactionDescription(transactionDescription);
        transaction.setTransactionReference(transactionReference);
        transaction.setTransactionDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        transaction.setProcessingBank(processingBank);
        transaction.setCounterpartyBankName(counterpartyBank);

        account = addTransactionToAccount(account, transaction);
        account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        account = createTransactionFeesTransaction(account, amount);

        accountService.updateAccount(account);
        notificationService.NotifyAccountHolder("R " + transaction.getAmount() + " has been debited from your account with account number: " + account.getAccountNumber() +
                "\nTransaction description: " + transaction.getTransactionDescription() + "\nTransaction reference: " + transaction.getTransactionReference());

        return Mapping.toTransaction(transaction);
    }

    public Transaction creditAccount(String accountNumber, BigDecimal amount, String processingBank, String counterpartyBank, String transactionReference, String transactionDescription) {
        AccountEntity account = accountService.findAccountEntityByAccountNumber(accountNumber);

        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Invalid amount supplied for credit transaction, please provide a valid amount");
        }

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setTransactionType("CREDIT");
        transaction.setAmount(amount);
        transaction.setTransactionDescription(transactionDescription);
        transaction.setTransactionReference(transactionReference);
        transaction.setTransactionDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        transaction.setProcessingBank(processingBank);
        transaction.setCounterpartyBankName(counterpartyBank);

        account = addTransactionToAccount(account, transaction);
        account.setBalance(account.getBalance().add(transaction.getAmount()));

        accountService.updateAccount(account);
        notificationService.NotifyAccountHolder("R " + transaction.getAmount() + " has been credited to your account with account number: " + account.getAccountNumber() +
                "\nTransaction description: " + transaction.getTransactionDescription() + "\nTransaction reference: " + transaction.getTransactionReference());

        return Mapping.toTransaction(transaction);
    }

    //    public Transaction processInternalTransaction(Long payFromAccountId, Long payToAccountId, BigDecimal amount, String description) {
//
//    }

    //notificationService.NotifyAccountHolder("Transaction fee of R " + transaction.getAmount() + " has been debited from your account with account number: " + account.getAccountNumber());
    //notificationService.NotifyAccountHolder("Savings interest of R " + transaction.getAmount() + " has been credited to your account with account number: " + account.getAccountNumber());
}

