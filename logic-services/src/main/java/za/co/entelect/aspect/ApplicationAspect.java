package za.co.entelect.aspect;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import za.co.entelect.annotation.NotifyCustomer;
import za.co.entelect.dto.Account;
import za.co.entelect.dto.CreateTransaction;
import za.co.entelect.dto.Customer;
import za.co.entelect.dto.Transaction;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.entity.TransactionEntity;
import za.co.entelect.service.AccountService;
import za.co.entelect.service.CustomerService;
import za.co.entelect.service.NotificationService;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Aspect
@Component
@Slf4j
public class ApplicationAspect {

    private final NotificationService notificationService;

    @Autowired
    public ApplicationAspect(NotificationService notificationService) {
        this.notificationService = notificationService;
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

    @Around("@annotation(za.co.entelect.annotation.ValidateTransfer)")
    public Object validateTransfer(ProceedingJoinPoint joinPoint) throws Throwable{

        Object[] args = joinPoint.getArgs();

        if (args.length < 5
                || !(args[0] instanceof CreateTransaction transactionDetails)
                || !(args[1] instanceof Customer payingCustomer)
                || !(args[3] instanceof AccountEntity fromAccount)
                || !(args[4] instanceof AccountEntity toAccount)) {
            throw new IllegalArgumentException("Expected method parameter mismatch");
        }

        boolean isPaymentToOtherCustomer = (boolean) args[2];

        if (isPaymentToOtherCustomer) {
            validateOwnershipOfAccount(fromAccount, payingCustomer);
            validateAvailableBalance(fromAccount, transactionDetails.getAmount());
        } else {
            validateOwnershipOfTransferAccounts(fromAccount, toAccount, payingCustomer);
            validateAvailableBalanceOfTransferAccount(fromAccount, transactionDetails.getAmount());
        }
        validateAmount(transactionDetails.getAmount());
        validateSavingAccountConstraints(fromAccount, toAccount, false);

        return joinPoint.proceed();
    }

    @Around("@annotation(za.co.entelect.annotation.ValidateExternalPayment)")
    public Object validateExternalPayment(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        if (args.length < 4
                || !(args[0] instanceof CreateTransaction transactionDetails)
                || !(args[1] instanceof LocalDateTime)
                || !(args[3] instanceof AccountEntity localAccount)) {
            throw new IllegalArgumentException("Expected method parameter mismatch");
        }

        boolean isDebitTransaction = (boolean) args[2];

        validateAmount(transactionDetails.getAmount());
        if (isDebitTransaction) {
            validateAvailableBalance(localAccount, transactionDetails.getAmount());
            validateSavingAccountConstraints(localAccount, null, true);
        }

        return joinPoint.proceed();
    }

    @AfterReturning(value = "@annotation(annotation) && execution(public * za.co.entelect.service.TransactionService.*Account(..))", returning = "result")
    public void handleTransactionReturn(JoinPoint joinPoint, Object result, NotifyCustomer annotation) {
        if (!(result instanceof Transaction)) {
            throw new IllegalArgumentException("Expected return type of Transaction");
        }

        processTransaction(joinPoint, (Transaction) result, annotation);
    }
    private void processTransaction(JoinPoint joinPoint, Transaction transaction, NotifyCustomer annotation) {
        Object[] args = joinPoint.getArgs();
        if (args.length < 3 || !(args[0] instanceof CreateTransaction) || !(args[1] instanceof AccountEntity) || !(args[2] instanceof LocalDateTime)) {
            throw new IllegalArgumentException("Expected method parameter mismatch");
        }

        AccountEntity account = (AccountEntity) args[1];

        switch (annotation.value()) {
            case "credit-transaction":
                notificationService.NotifyAccountHolder("R " + transaction.getAmount() + " has been credited to your account with account number: "
                        + account.getAccountNumber() + "\nTransaction description: " + transaction.getTransactionDescription() +
                        "\nTransaction reference: " + transaction.getTransactionReference());
                break;
            case "debit-transaction":
                notificationService.NotifyAccountHolder("R " + transaction.getAmount() + " has been debited from your account with account number: "
                        + account.getAccountNumber() + "\nTransaction description: " + transaction.getTransactionDescription() +
                        "\nTransaction reference: " + transaction.getTransactionReference());
                break;
            default:
                throw new IllegalArgumentException("Invalid customer notification type");
        }
    }

    @After("@annotation(annotation) && execution(* za.co.entelect.service.TransactionService.create*Transaction(..))")
    public void notifyCustomerOfInternalTransaction(NotifyCustomer annotation, JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length < 2 || !(args[0] instanceof AccountEntity) || !(args[1] instanceof BigDecimal)) {
            throw new IllegalArgumentException("Expected method parameter mismatch");
        }

        AccountEntity account = (AccountEntity) args[0];
        BigDecimal amount = (BigDecimal) args[1];
        switch (annotation.value()) {
            case "fee-transaction":
                notificationService.NotifyAccountHolder("Transaction fee of R " + calculateTransactionFee(amount) +
                        " has been debited from your account with account number: " + account.getAccountNumber());
                break;
            case "interest-transaction":
                notificationService.NotifyAccountHolder("Savings interest of R " + calculateInterest(amount)
                        + " has been credited to your account with account number: " + account.getAccountNumber());
                break;
            default:
                throw new IllegalArgumentException("Invalid customer notification type");
        }
    }

    @AfterReturning(value = "@annotation(annotation) && execution(public * za.co.entelect.service.TransactionService.createSignUpBonusTransaction(..))",
            returning = "returnAccount")
    public void notifyCustomerOfSignupTransaction(NotifyCustomer annotation, AccountEntity returnAccount){
        final BigDecimal joiningBonus = BigDecimal.valueOf(500.00);

        if((annotation.value()).equals("signup-bonus-transaction")){
            notificationService.NotifyAccountHolder("Joining bonus of R " + joiningBonus +
                    " has been credited to to your account with account number: " + returnAccount.getAccountNumber());
        }
        else{
            throw new IllegalArgumentException("Invalid customer notification type");
        }
    }

//    @Around("execution(* za.co.entelect.service.*.*(..))")
//    public Object logMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
//        long start = System.currentTimeMillis();
//        Object result = joinPoint.proceed();
//        long executedTime = System.currentTimeMillis() - start;
//        log.info("{} executed in {}ms", joinPoint.getSignature(), executedTime);
//        return result;
//    }
}
