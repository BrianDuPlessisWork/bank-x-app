package za.co.entelect.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.entelect.dto.CreateTransaction;
import za.co.entelect.dto.Customer;
import za.co.entelect.dto.Transaction;
import za.co.entelect.entity.AccountEntity;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class PaymentService {

    private final AccountService accountService;
    private final TransferService transferService;

    @Autowired
    public PaymentService(AccountService accountService, TransferService transferService) {
        this.accountService = accountService;
        this.transferService = transferService;
    }

    private Optional<AccountEntity> findAccountSafely(Supplier<AccountEntity> accountSupplier) {
        try {
            return Optional.of(accountSupplier.get());
        } catch (EntityNotFoundException e) {
            return Optional.empty();
        }
    }

    private boolean isDebitTransaction(CreateTransaction transactionDetails){
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

    public Transaction processInternalTransfer(CreateTransaction transactionDetails, Customer payingCustomer) throws AccessDeniedException {
        return callProcessTransfer(transactionDetails, payingCustomer, false);
    }

    public Transaction processInternalPayment(CreateTransaction transactionDetails, Customer payingCustomer) throws AccessDeniedException {
        return callProcessTransfer(transactionDetails, payingCustomer, true);
    }

    public Transaction callProcessTransfer(CreateTransaction transactionDetails, Customer payingCustomer, boolean isPaymentToOtherCustomer)
            throws AccessDeniedException {

        AccountEntity fromAccount = accountService.findAccountEntityByAccountNumber(transactionDetails.getPayFromAccountNumber());
        AccountEntity toAccount = accountService.findAccountEntityByAccountNumber(transactionDetails.getPayToAccountNumber());

        return transferService.processTransfer(transactionDetails, payingCustomer, isPaymentToOtherCustomer, fromAccount, toAccount);
    }

    public Transaction callProcessSingleExternalPayment(CreateTransaction transactionDetails, LocalDateTime timestamp) {
        Transaction returnTransaction;
        AccountEntity localAccount;
        boolean isDebitTransaction = isDebitTransaction(transactionDetails);

        if (isDebitTransaction){
            localAccount = accountService.findAccountEntityByAccountNumber(transactionDetails.getPayFromAccountNumber());
        }
        else{
            localAccount = accountService.findAccountEntityByAccountNumber(transactionDetails.getPayToAccountNumber());
        }
        return transferService.processSingleExternalPayment(transactionDetails, timestamp, isDebitTransaction, localAccount);
    }

    @Transactional
    public List<Transaction> processMultipleExternalPayments(List<CreateTransaction> transactionDetailList) {
        List<Transaction> returnTransactionList = new ArrayList<>();
        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        for (CreateTransaction transactionDetail: transactionDetailList){
            returnTransactionList.add(callProcessSingleExternalPayment(transactionDetail, timestamp));
        }
        return returnTransactionList;
    }
}
