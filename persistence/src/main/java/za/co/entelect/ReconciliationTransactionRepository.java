package za.co.entelect;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.entelect.entity.ReconciliationTransactionEntity;

@Repository
public interface ReconciliationTransactionRepository extends JpaRepository<ReconciliationTransactionEntity, Long> {

}
