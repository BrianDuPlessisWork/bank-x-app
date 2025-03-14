package za.co.entelect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.entelect.dto.Customer;
import za.co.entelect.dto.CreateTransaction;
import za.co.entelect.dto.Transaction;
import za.co.entelect.service.CustomerService;
import za.co.entelect.service.TransactionService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CustomerService customerService;

    @Autowired
    public TransactionController(TransactionService transactionService, CustomerService customerService) {
        this.transactionService = transactionService;
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountNumber(
            @RequestParam(name = "accountNumber") String accountNumber,
            @PathVariable(name = "customerId") Long customerID) throws AccessDeniedException {

        Customer customer = customerService.getCustomerByCustomerId(customerID);
        List<Transaction> transactionList = transactionService.getTransactionsByAccountNumber(accountNumber, customer);
        return ResponseEntity.ok(transactionList);
    }

    @PostMapping("/internal/transfer/{customerId}")
    public ResponseEntity<Transaction> transferBetweenInternalAccounts(
            @PathVariable(name= "customerId") Long customerId,
            @RequestBody CreateTransaction createTransactionDto) throws AccessDeniedException {

        Customer customer = customerService.getCustomerByCustomerId(customerId);
        Transaction transaction = transactionService.processInternalTransfer(createTransactionDto, customer);

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/internal/payment/{customerId}")
    public ResponseEntity<Transaction> makeInternalPayment (
            @PathVariable(name= "customerId") Long customerId,
            @RequestBody CreateTransaction createTransactionDto) throws AccessDeniedException {

        Customer customer = customerService.getCustomerByCustomerId(customerId);
        Transaction transaction = transactionService.processInternalPayment(createTransactionDto, customer);

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/external/payment/single")
    public ResponseEntity<Transaction> makeSingleExternalPayment (@RequestBody CreateTransaction createTransactionDto) {

        Transaction transaction = transactionService.processSingleExternalPayment(createTransactionDto, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/external/payment/multiple")
    public ResponseEntity<List<Transaction>> makeMultipleExternalPayment (@RequestBody List<CreateTransaction> createTransactionDtoList) {

        List<Transaction> transactionList = transactionService.processMultipleExternalPayments(createTransactionDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionList);
    }
}
