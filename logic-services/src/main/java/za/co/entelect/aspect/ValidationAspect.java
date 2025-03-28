package za.co.entelect.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import za.co.entelect.dto.CreateTransaction;
import za.co.entelect.dto.Customer;
import za.co.entelect.entity.AccountEntity;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Aspect
@Component
public class ValidationAspect {

    private BigDecimal calculateTransactionFee(BigDecimal amount){
        final BigDecimal transactionFeePercentage = BigDecimal.valueOf(0.0005);
        return amount.multiply(transactionFeePercentage);
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
}
