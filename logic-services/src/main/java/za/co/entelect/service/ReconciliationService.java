package za.co.entelect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.entelect.ReconciliationBatchRepository;
import za.co.entelect.ReconciliationTransactionRepository;
import za.co.entelect.dto.ReconciliationTransaction;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.entity.ReconciliationBatchEntity;
import za.co.entelect.entity.ReconciliationTransactionEntity;
import za.co.entelect.entity.TransactionEntity;
import za.co.entelect.utility.Mapping;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReconciliationService {
    
    private final ReconciliationTransactionRepository reconciliationTransactionRepository;
    private final ReconciliationBatchRepository reconciliationBatchRepository;
    private final TransactionService transactionService;

    @Autowired
    public ReconciliationService(ReconciliationTransactionRepository reconciliationTransactionRepository, ReconciliationBatchRepository reconciliationBatchRepository, TransactionService transactionService) {
        this.reconciliationTransactionRepository = reconciliationTransactionRepository;
        this.reconciliationBatchRepository = reconciliationBatchRepository;
        this.transactionService = transactionService;
    }

    public List<ReconciliationTransaction> captureReconciliationTransactions(
            List<ReconciliationTransactionEntity> reconciliationTransactionEntityList, String processingBank){
        List<ReconciliationTransactionEntity> savedTransactionList = new ArrayList<>();

        ReconciliationBatchEntity reconciliationBatch = new ReconciliationBatchEntity();
        reconciliationBatch.setStatus("PROCESSING_PENDING");
        reconciliationBatch.setSubmissionDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        reconciliationBatch.setProcessingBank(processingBank);
        
        for (ReconciliationTransactionEntity transaction : reconciliationTransactionEntityList){
            ReconciliationTransactionEntity reconciliationTransaction = getReconciliationTransactionEntity(transaction, reconciliationBatch);
            savedTransactionList.add(reconciliationTransactionRepository.save(reconciliationTransaction));
        }

        return savedTransactionList.stream().map(Mapping::toReconciliationTransaction).toList();
    }

    private static ReconciliationTransactionEntity getReconciliationTransactionEntity(ReconciliationTransactionEntity transaction,
                                                                                      ReconciliationBatchEntity reconciliationBatch) {
        ReconciliationTransactionEntity reconciliationTransaction = new ReconciliationTransactionEntity();
        reconciliationTransaction.setReconciliationBatch(reconciliationBatch);
        reconciliationTransaction.setAccountNumber(transaction.getAccountNumber());
        reconciliationTransaction.setTransactionType(transaction.getAccountType());
        reconciliationTransaction.setBranchCode(transaction.getBranchCode());
        reconciliationTransaction.setTransactionReference(transaction.getTransactionReference());
        reconciliationTransaction.setTransactionType(transaction.getTransactionType());
        reconciliationTransaction.setCounterpartyBankName(transaction.getCounterpartyBankName());
        reconciliationTransaction.setStatus(transaction.getStatus());
        return reconciliationTransaction;
    }

    private boolean isTransactionMatch(TransactionEntity transaction, ReconciliationTransactionEntity reconciliationTransaction) {
        AccountEntity account = transaction.getAccount();

        return  transaction.getAmount().compareTo(reconciliationTransaction.getAmount()) == 0 &&
                transaction.getTransactionType().equals(reconciliationTransaction.getTransactionType()) &&
                transaction.getTransactionDate().equals(reconciliationTransaction.getTransactionDate()) &&
                transaction.getCounterpartyBankName().equals(reconciliationTransaction.getCounterpartyBankName()) &&
                account.getAccountNumber().equals(reconciliationTransaction.getAccountNumber()) &&
                account.getAccountType().equals(reconciliationTransaction.getAccountType()) &&
                account.getBranchCode().equals(reconciliationTransaction.getBranchCode());
    }

    @Transactional
    private void processReconciliationTransactions(){
        List<ReconciliationBatchEntity> batchesToProcess = reconciliationBatchRepository.findByStatus("PROCESSING_PENDING");

        for (ReconciliationBatchEntity reconciliationBatch: batchesToProcess){
            if (reconciliationBatch != null) {
                List<ReconciliationTransactionEntity> reconciliationTransactions = reconciliationTransactionRepository
                        .findByBatchNumber(reconciliationBatch.getReconciliationBatchID());

                for (ReconciliationTransactionEntity reconciliationTransaction : reconciliationTransactions) {
                    Optional<TransactionEntity> capturedTransaction =
                            transactionService.findByTransactionReference(reconciliationTransaction.getTransactionReference());

                    if (capturedTransaction.isPresent()) {
                        TransactionEntity transaction = capturedTransaction.get();
                        boolean isMatch = isTransactionMatch(transaction, reconciliationTransaction);
                        reconciliationTransaction.setStatus(isMatch ? "MATCHED" : "NOT_MATCHED");
                    }
                    else{
                        reconciliationTransaction.setStatus("NOT_FOUND");
                    }
                    reconciliationTransactionRepository.save(reconciliationTransaction);
                }
                reconciliationBatch.setStatus("PROCESSED");
                reconciliationBatchRepository.save(reconciliationBatch);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void executeReconciliation(){
        try{
            processReconciliationTransactions();
        } catch (RuntimeException e){
            throw new RuntimeException("Could not successfully execute scheduled reconciliation transaction processing", e);
        }
    }
}
