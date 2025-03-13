package za.co.entelect;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.entelect.entity.ReconciliationBatchEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReconciliationBatchRepository extends JpaRepository<ReconciliationBatchEntity, Long> {
    List<ReconciliationBatchEntity> findByStatus(String status);
}
