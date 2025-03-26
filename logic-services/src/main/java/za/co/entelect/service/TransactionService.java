package za.co.entelect.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.entelect.TransactionRepository;
import za.co.entelect.annotation.NotifyCustomer;
import za.co.entelect.annotation.ValidateExternalPayment;
import za.co.entelect.annotation.ValidateTransfer;
import za.co.entelect.dto.Account;
import za.co.entelect.dto.Customer;
import za.co.entelect.dto.CreateTransaction;
import za.co.entelect.dto.Transaction;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.entity.TransactionEntity;
import za.co.entelect.utility.Mapping;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

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

    public Optional<TransactionEntity> findByTransactionId(Long transactionId){
        return transactionRepository.findById(transactionId);
    }

    public List<Transaction> getTransactionsByAccountNumber(String accountNumber, Customer customer) throws AccessDeniedException {
        Account account = accountService.findAccountByAccountNumber(accountNumber);

        if (!account.getCustomer().getCustomerID().equals(customer.getCustomerID())) {
            throw new AccessDeniedException("The customer does not own the specified account");
        }

        List<TransactionEntity> transactionEntityList = transactionRepository.findByAccountNumber(accountNumber);

        return transactionEntityList.stream().map(Mapping::toTransaction).toList();
    }

    @NotifyCustomer("signup-bonus-transaction")
    public AccountEntity createSignUpBonusTransaction(AccountEntity account){
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

        return accountService.updateAccount(account);
    }

    @NotifyCustomer("fee-transaction")
    public void createTransactionFeesTransaction(AccountEntity account, BigDecimal amount){
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

        accountService.updateAccount(account);
    }

    @NotifyCustomer("interest-transaction")
    public void createSavingsInterestTransaction(AccountEntity account, BigDecimal amount){
        final BigDecimal interestAmount = calculateInterest(amount);

        if (!(account.getAccountType()).equals("SAVINGS")){
            throw new IllegalArgumentException("Only a savings account can occur interest");
        }

        if((account.getBalance()).compareTo(BigDecimal.ZERO) >= 1){
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

            accountService.updateAccount(account);
            notificationService.NotifyAccountHolder("Savings interest of R " + calculateInterest(amount)
                    + " has been credited to your account with account number: " + account.getAccountNumber());
        }
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
        final BigDecimal transactionFeePercentage = BigDecimal.valueOf(0.0005);
        return amount.multiply(transactionFeePercentage);
    }

    private BigDecimal calculateInterest(BigDecimal amount){
        final BigDecimal interestPercentage = BigDecimal.valueOf(0.005);
        return (amount).multiply(interestPercentage);
    }

    @NotifyCustomer("debit-transaction")
    public Transaction debitAccount(CreateTransaction transactionDetails, AccountEntity account, LocalDateTime timestamp) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setTransactionType("DEBIT");
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setTransactionDescription(transactionDetails.getDescription());
        transaction.setTransactionReference(transactionDetails.getPayToAccountNumber());
        transaction.setTransactionDate(timestamp);
        transaction.setProcessingBank(transactionDetails.getProcessingBank());
        transaction.setCounterpartyBankName(transactionDetails.getCounterpartyBank());

        account = addTransactionToAccount(account, transaction);
        account.setBalance(account.getBalance().subtract(transaction.getAmount()));

        AccountEntity updatedAccount = accountService.updateAccount(account);

        TransactionEntity savedTransaction = updatedAccount.getTransactions()
                .stream()
                .filter(t -> t.getTransactionDate().equals(timestamp) &&
                        t.getAmount().equals(transactionDetails.getAmount()) &&
                        t.getTransactionDescription().equals(transactionDetails.getDescription()))
                .findFirst()
                .orElse(transaction);
        return Mapping.toTransaction(savedTransaction);
    }

    @NotifyCustomer("credit-transaction")
    public Transaction creditAccount(CreateTransaction transactionDetails, AccountEntity account, LocalDateTime timestamp) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setTransactionType("CREDIT");
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setTransactionDescription(transactionDetails.getDescription());
        transaction.setTransactionReference(transactionDetails.getPayFromAccountNumber());
        transaction.setTransactionDate(timestamp);
        transaction.setProcessingBank(transactionDetails.getProcessingBank());
        transaction.setCounterpartyBankName(transactionDetails.getCounterpartyBank());

        account = addTransactionToAccount(account, transaction);
        account.setBalance(account.getBalance().add(transaction.getAmount()));

        AccountEntity updatedAccount = accountService.updateAccount(account);

        TransactionEntity savedTransaction = updatedAccount.getTransactions()
                .stream()
                .filter(t -> t.getTransactionDate().equals(timestamp) &&
                        t.getAmount().equals(transactionDetails.getAmount()) &&
                        t.getTransactionDescription().equals(transactionDetails.getDescription()))
                .findFirst()
                .orElse(transaction);
        return Mapping.toTransaction(savedTransaction);
    }
}

