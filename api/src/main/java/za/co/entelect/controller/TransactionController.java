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
            @RequestBody MakeTransaction makeTransactionDto) {

        return ResponseEntity.ok(null);
    }

    @PostMapping("/internal/payment/{customerId}")
    public ResponseEntity<Transaction> makeInternalPayment (
            @PathVariable(name= "customerId") Long customerId,
            @RequestBody MakeTransaction makeTransactionDto) {

        return ResponseEntity.ok(null);
    }

    @PostMapping("/external/payment/single")
    public ResponseEntity<Transaction> makeSingleExternalPayment (@RequestBody MakeTransaction makeTransactionDto) {

        return ResponseEntity.ok(null);
    }

    @PostMapping("/external/payment/multiple")
    public ResponseEntity<List<Transaction>> makeMultipleExternalPayment (@RequestBody List<MakeTransaction> makeTransactionDtoList) {
        return ResponseEntity.ok(null);
    }
}

//@PostMapping("/external/debit")
//public ResponseEntity<Transaction> debitInternalAccount(
//        @RequestParam(name= "customerId") Long customerId,
//        @RequestParam(name= "payFromAccountNumber") String payFromAccountNumber,
//        @RequestParam(name= "payToAccountNumber") String payToAccountNumber,
//        @RequestParam(name= "amount") BigDecimal amount,
//        @RequestParam(name= "processingBank") String processingBank,
//        @RequestParam(name= "counterpartyBank") String counterpartyBank,
//        @RequestParam(name= "TransactionDescription") String description) throws AccessDeniedException {
//
//    Customer payingCustomer = customerService.getCustomerByCustomerId(customerId);
//    Transaction customerTransaction = transactionService.debitAccount(payFromAccountNumber, payingCustomer, amount,
//            processingBank, counterpartyBank, payToAccountNumber, description, true);
//
//    return ResponseEntity.ok(customerTransaction);
//}
//
//@PostMapping("/external/credit")
//public ResponseEntity<Transaction> creditInternalAccount(
//        @RequestParam(name= "payFromAccountNumber") String payFromAccountNumber,
//        @RequestParam(name= "payToAccountNumber") String payToAccountNumber,
//        @RequestParam(name= "amount") BigDecimal amount,
//        @RequestParam(name= "processingBank") String processingBank,
//        @RequestParam(name= "counterpartyBank") String counterpartyBank,
//        @RequestParam(name= "TransactionDescription") String description) {
//
//    Transaction customerTransaction = transactionService.creditAccount(payToAccountNumber, amount,
//            processingBank, counterpartyBank, payFromAccountNumber, description);
//
//    return ResponseEntity.ok(customerTransaction);
//}