package za.co.entelect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.entelect.TransactionRepository;
import za.co.entelect.dto.Account;
import za.co.entelect.dto.Customer;
import za.co.entelect.dto.MakeTransaction;
import za.co.entelect.dto.Transaction;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.entity.TransactionEntity;
import za.co.entelect.utility.Mapping;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
        notificationService.NotifyAccountHolder("Transaction fee of R " + calculateTransactionFee(amount) +
                " has been debited from your account with account number: " + account.getAccountNumber());
    }

    public void createSavingsInterestTransaction(AccountEntity account, LocalDateTime timestamp, BigDecimal accountBalance){
        final BigDecimal interestAmount = calculateInterest(accountBalance);

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
            transaction.setTransactionDate(timestamp);
            transaction.setProcessingBank("BANK_X");
            transaction.setCounterpartyBankName("BANK_X");

            account = addTransactionToAccount(account, transaction);
            account.setBalance(account.getBalance().add(transaction.getAmount()));

            accountService.updateAccount(account);
            notificationService.NotifyAccountHolder("Savings interest of R " + calculateInterest(accountBalance)
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

    public void validateOwnershipOfTransferAccounts(AccountEntity fromAccount, AccountEntity toAccount, Customer customer) throws AccessDeniedException {
        if (!fromAccount.getCustomer().getCustomerID().equals(customer.getCustomerID()) || !toAccount.getCustomer().getCustomerID().equals(customer.getCustomerID())) {
            throw new AccessDeniedException("The customer does not own the specified accounts");
        }
    }

    public void validateOwnershipOfAccount(AccountEntity fromAccount, Customer customer) throws AccessDeniedException {
        if (!fromAccount.getCustomer().getCustomerID().equals(customer.getCustomerID())) {
            throw new AccessDeniedException("The customer does not own the specified account");
        }
    }

    public void validateAvailableBalanceOfTransferAccount(AccountEntity account, BigDecimal amount){
        if ((account.getBalance()).compareTo(amount) < 0) {
            throw new IllegalArgumentException("Customer does not have sufficient balance for the transfer");
        }
    }

    public void validateAvailableBalance(AccountEntity account, BigDecimal amount){
        if ((account.getBalance()).compareTo(amount.add(calculateTransactionFee(amount))) < 0) {
            throw new IllegalArgumentException("Customer does not have sufficient balance for the transfer");
        }
    }

    public void validateAmount(BigDecimal amount){
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Transaction can not be associated with a negative or zero amount, please provide a valid amount");
        }
    }

    public void validateSavingAccountConstraints(AccountEntity fromAccount, AccountEntity toAccount, boolean isExternalTransaction){
        if (((fromAccount.getAccountType()).equals("SAVINGS")) && isExternalTransaction) {
            throw new IllegalArgumentException("Savings account transfers can only be handled internally");
        }
        else if ((fromAccount.getAccountType().equals("SAVINGS")) &&
                (!toAccount.getAccountType().equals("CURRENT") ||
                        toAccount.getCustomer().getCustomerID() != fromAccount.getCustomer().getCustomerID())) {
            throw new IllegalArgumentException("Savings account can only transfer to accountholder's current account");
        }
    }

    private void validateTransactionDetails(MakeTransaction transactionDetails, Customer payingCustomer, boolean isPaymentToOtherCustomer, AccountEntity fromAccount, AccountEntity toAccount) throws AccessDeniedException {
        if (isPaymentToOtherCustomer) {
            validateOwnershipOfAccount(fromAccount, payingCustomer);
            validateAvailableBalance(fromAccount, transactionDetails.getAmount());
        } else {
            validateOwnershipOfTransferAccounts(fromAccount, toAccount, payingCustomer);
            validateAvailableBalanceOfTransferAccount(fromAccount, transactionDetails.getAmount());
        }
        validateAmount(transactionDetails.getAmount());
        validateSavingAccountConstraints(fromAccount, toAccount, false);
    }

    public Transaction debitAccount(MakeTransaction transactionDetails, AccountEntity account, LocalDateTime timestamp) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setTransactionType("DEBIT");
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setTransactionDescription(transactionDetails.getDescription());
        transaction.setTransactionReference(transactionDetails.getPayToAccountNumber());
        transaction.setTransactionDate(timestamp);
        transaction.setProcessingBank(transactionDetails.getProcessingBank());
        transaction.setCounterpartyBankName(transaction.getCounterpartyBankName());

        account = addTransactionToAccount(account, transaction);
        account.setBalance(account.getBalance().subtract(transaction.getAmount()));

        accountService.updateAccount(account);
        notificationService.NotifyAccountHolder("R " + transaction.getAmount() + " has been debited from your account with account number: "
                + account.getAccountNumber() + "\nTransaction description: " + transaction.getTransactionDescription() +
                "\nTransaction reference: " + transaction.getTransactionReference());

        return Mapping.toTransaction(transaction);
    }

    public Transaction creditAccount(MakeTransaction transactionDetails, AccountEntity account, LocalDateTime timestamp) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setTransactionType("CREDIT");
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setTransactionDescription(transactionDetails.getDescription());
        transaction.setTransactionReference(transactionDetails.getPayFromAccountNumber());
        transaction.setTransactionDate(timestamp);
        transaction.setProcessingBank(transactionDetails.getProcessingBank());
        transaction.setCounterpartyBankName(transaction.getCounterpartyBankName());

        account = addTransactionToAccount(account, transaction);
        account.setBalance(account.getBalance().add(transaction.getAmount()));

        accountService.updateAccount(account);
        notificationService.NotifyAccountHolder("R " + transaction.getAmount() + " has been credited to your account with account number: "
                + account.getAccountNumber() + "\nTransaction description: " + transaction.getTransactionDescription() +
                "\nTransaction reference: " + transaction.getTransactionReference());

        return Mapping.toTransaction(transaction);
    }

    private Transaction processTransfer(MakeTransaction transactionDetails, Customer payingCustomer, boolean isPaymentToOtherCustomer)
            throws AccessDeniedException {

        AccountEntity fromAccount = accountService.findAccountEntityByAccountNumber(transactionDetails.getPayFromAccountNumber());
        AccountEntity toAccount = accountService.findAccountEntityByAccountNumber(transactionDetails.getPayToAccountNumber());
        BigDecimal toAccountBalanceBeforeTransaction = toAccount.getBalance();

        LocalDateTime transactionTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        validateTransactionDetails(transactionDetails, payingCustomer, isPaymentToOtherCustomer, fromAccount, toAccount);

        Transaction debitTransaction = debitAccount(transactionDetails, fromAccount, transactionTime);
        creditAccount(transactionDetails, toAccount, transactionTime);

        if (toAccount.getAccountType().equals("SAVINGS")) {
            createSavingsInterestTransaction(toAccount, transactionTime, toAccountBalanceBeforeTransaction);
        }
        if (isPaymentToOtherCustomer) {
            createTransactionFeesTransaction(fromAccount, transactionDetails.getAmount());
        }

        return debitTransaction;
    }

    private Optional<AccountEntity> findAccountSafely(Supplier<AccountEntity> accountSupplier) {
        try {
            return Optional.of(accountSupplier.get());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private boolean isDebitTransaction(MakeTransaction transactionDetails){
        Optional<AccountEntity> payFromAccount = findAccountSafely(
                () -> accountService.findAccountEntityByAccountNumber(transactionDetails.getPayFromAccountNumber()));

        if (payFromAccount.isPresent()) {
            return true;
        } else {
            Optional<AccountEntity> payToAccount = findAccountSafely(
                    () -> accountService.findAccountEntityByAccountNumber(transactionDetails.getPayToAccountNumber()));

            if (payToAccount.isPresent()) {
                return false;
            } else {
                throw new IllegalArgumentException("None of the provided accounts are owned by Bank X");
            }
        }
    }

    @Transactional
    public Transaction processInternalTransfer(MakeTransaction transactionDetails, Customer payingCustomer) throws AccessDeniedException {
        return processTransfer(transactionDetails, payingCustomer, false);
    }

    @Transactional
    public Transaction processInternalPayment(MakeTransaction transactionDetails, Customer payingCustomer) throws AccessDeniedException {
        return processTransfer(transactionDetails, payingCustomer, true);
    }

    @Transactional
    public Transaction processSingleExternalPayment(MakeTransaction transactionDetails, LocalDateTime timestamp) {
        Transaction returnTransaction;
        AccountEntity localAccount;
        boolean isDebitTransaction = isDebitTransaction(transactionDetails);

        validateAmount(transactionDetails.getAmount());
        if (isDebitTransaction){
            localAccount = accountService.findAccountEntityByAccountNumber(transactionDetails.getPayFromAccountNumber());

            validateAvailableBalance(localAccount, transactionDetails.getAmount());
            validateSavingAccountConstraints(localAccount, null, true);

            returnTransaction = debitAccount(transactionDetails, localAccount, timestamp);
            createTransactionFeesTransaction(localAccount, transactionDetails.getAmount());
        }
        else{
            localAccount = accountService.findAccountEntityByAccountNumber(transactionDetails.getPayToAccountNumber());
            BigDecimal accountBalance = localAccount.getBalance();
            returnTransaction = creditAccount(transactionDetails, localAccount, timestamp);
            if ((localAccount.getAccountType()).equals("SAVINGS")){
                createSavingsInterestTransaction(localAccount, timestamp, accountBalance);
            }
        }
        return returnTransaction;
    }

    @Transactional
    public List<Transaction> processMultipleExternalPayments(List<MakeTransaction> transactionDetailList) {
        List<Transaction> returnTransactionList = new ArrayList<>();
        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        for (MakeTransaction transactionDetail: transactionDetailList){
            returnTransactionList.add(processSingleExternalPayment(transactionDetail, timestamp));
        }
        return returnTransactionList;
    }
}

