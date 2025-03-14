package za.co.entelect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.entelect.dto.ReconciliationTransaction;
import za.co.entelect.dto.Transaction;
import za.co.entelect.service.ReconciliationService;

import java.util.List;

@RestController
@RequestMapping("api/reconciliations")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @Autowired
    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/{processingBank}")
    public ResponseEntity<List<ReconciliationTransaction>> captureReconciliationTransactions(
            @RequestBody List<Transaction> reconciliationTransactionList,
            @PathVariable(name= "processingBank") String processingBank){

        List<ReconciliationTransaction> transactionList = reconciliationService.captureReconciliationTransactions(reconciliationTransactionList, processingBank);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionList);
    }
}
