package za.co.entelect.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import za.co.entelect.annotation.NotifyCustomer;
import za.co.entelect.dto.CreateTransaction;
import za.co.entelect.dto.Transaction;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Aspect
@Component
public class NotificationAspect {

    private final NotificationService notificationService;

    @Autowired
    public NotificationAspect(NotificationService notificationService) {
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
}
