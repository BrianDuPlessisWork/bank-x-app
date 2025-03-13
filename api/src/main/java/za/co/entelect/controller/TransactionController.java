package za.co.entelect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.entelect.dto.Customer;
import za.co.entelect.dto.MakeTransaction;
import za.co.entelect.dto.Transaction;
import za.co.entelect.service.CustomerService;
import za.co.entelect.service.TransactionService;

import java.math.BigDecimal;
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

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactionsByAccountAndDateRange(
            @RequestParam(name = "accountNumber") String accountNumber,
            @RequestParam(name = "customerID") Long customerID) throws AccessDeniedException {

        Customer customer = customerService.getCustomerByCustomerId(customerID);
        List<Transaction> transactionList = transactionService.getTransactionsByAccountNumber(accountNumber, customer);
        return ResponseEntity.ok(transactionList);
    }

    @PostMapping("/internal/transfer/{customerId}")
    public ResponseEntity<Transaction> transferBetweenInternalAccounts(
            @PathVariable(name= "customerId") Long customerId,
            @RequestBody MakeTransaction makeTransactionDto) throws AccessDeniedException {

        Customer customer = customerService.getCustomerByCustomerId(customerId);
        Transaction transaction = transactionService.processInternalTransfer(makeTransactionDto, customer);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/internal/payment/{customerId}")
    public ResponseEntity<Transaction> makeInternalPayment (
            @PathVariable(name= "customerId") Long customerId,
            @RequestBody MakeTransaction makeTransactionDto) throws AccessDeniedException {

        Customer customer = customerService.getCustomerByCustomerId(customerId);
        Transaction transaction = transactionService.processInternalPayment(makeTransactionDto, customer);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/external/payment/single")
    public ResponseEntity<Transaction> makeSingleExternalPayment (@RequestBody MakeTransaction makeTransactionDto) {

        Transaction transaction = transactionService.processSingleExternalPayment(makeTransactionDto, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/external/payment/multiple")
    public ResponseEntity<List<Transaction>> makeMultipleExternalPayment (@RequestBody List<MakeTransaction> makeTransactionDtoList) {

        List<Transaction> transactionList = transactionService.processMultipleExternalPayments(makeTransactionDtoList);
        return ResponseEntity.ok(transactionList);
    }
}
