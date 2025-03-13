package za.co.entelect;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.entelect.entity.ReconciliationTransactionEntity;
import za.co.entelect.entity.TransactionEntity;

import java.util.List;

@Repository
public interface ReconciliationTransactionRepository extends JpaRepository<ReconciliationTransactionEntity, Long> {
    @Query("SELECT rt FROM ReconciliationTransactionEntity rt WHERE rt.reconciliationBatch.reconciliationBatchID = :reconciliationBatchId")
    List<ReconciliationTransactionEntity> findByBatchNumber(@Param("reconciliationBatchId") Long reconciliationBatchId);
}
