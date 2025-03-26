package za.co.entelect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.entelect.annotation.ValidateExternalPayment;
import za.co.entelect.annotation.ValidateTransfer;
import za.co.entelect.dto.CreateTransaction;
import za.co.entelect.dto.Customer;
import za.co.entelect.dto.Transaction;
import za.co.entelect.entity.AccountEntity;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class TransferService {

    private final TransactionService transactionService;

    @Autowired
    public TransferService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Transactional
    @ValidateTransfer
    public Transaction processTransfer(CreateTransaction transactionDetails,
                                       Customer payingCustomer,
                                       boolean isPaymentToOtherCustomer,
                                       AccountEntity fromAccount,
                                       AccountEntity toAccount)
            throws AccessDeniedException {

        BigDecimal toAccountBalanceBeforeTransaction = toAccount.getBalance();
        LocalDateTime transactionTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Transaction debitTransaction = transactionService.debitAccount(transactionDetails, fromAccount, transactionTime);
        transactionService.creditAccount(transactionDetails, toAccount, transactionTime);

        if (toAccount.getAccountType().equals("SAVINGS")) {
            transactionService.createSavingsInterestTransaction(toAccount, toAccountBalanceBeforeTransaction);
        }
        if (isPaymentToOtherCustomer) {
            transactionService.createTransactionFeesTransaction(fromAccount, transactionDetails.getAmount());
        }

        return debitTransaction;
    }

    @Transactional
    @ValidateExternalPayment
    public Transaction processSingleExternalPayment(CreateTransaction transactionDetails, LocalDateTime timestamp, boolean isDebitTransaction, AccountEntity localAccount) {
        Transaction returnTransaction;

        if (isDebitTransaction){
            returnTransaction = transactionService.debitAccount(transactionDetails, localAccount, timestamp);
            transactionService.createTransactionFeesTransaction(localAccount, transactionDetails.getAmount());
        }
        else{
            BigDecimal accountBalance = localAccount.getBalance();
            returnTransaction = transactionService.creditAccount(transactionDetails, localAccount, timestamp);
            if ((localAccount.getAccountType()).equals("SAVINGS")){
                transactionService.createSavingsInterestTransaction(localAccount, accountBalance);
            }
        }
        return returnTransaction;
    }
}
