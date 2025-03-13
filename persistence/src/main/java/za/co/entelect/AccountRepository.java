package za.co.entelect;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.entelect.entity.AccountEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    @Query("SELECT a FROM AccountEntity a WHERE a.customer.customerID = :customerId")
    Optional<List<AccountEntity>> findByCustomerId(@Param("customerId") Long customerId);
}
