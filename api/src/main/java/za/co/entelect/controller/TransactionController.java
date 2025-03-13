package za.co.entelect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.entelect.dto.Customer;
import za.co.entelect.dto.Transaction;
import za.co.entelect.service.CustomerService;
import za.co.entelect.service.TransactionService;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
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

    @PostMapping("/internal/transfer")
    public ResponseEntity<Transaction> transferBetweenInternalAccounts(
            @RequestParam(name= "payFromAccountId") Long payFromAccountId,
            @RequestParam(name= "payToAccountId") Long payToAccountId,
            @RequestParam(name= "amount") BigDecimal amount,
            @RequestParam(name= "TransactionDescription") String description) {
        return ResponseEntity.ok(null);
    }

}
