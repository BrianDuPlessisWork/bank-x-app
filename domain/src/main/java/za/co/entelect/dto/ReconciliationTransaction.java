package za.co.entelect.dto;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import za.co.entelect.entity.ReconciliationBatchEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ReconciliationTransaction {
    private Long reconciliationTransactionID;
    private String accountNumber;
    private String accountType;
    private String branchCode;
    private BigDecimal amount;
    private String transactionReference;
    private String transactionType;
    private LocalDateTime transactionDate;
    private String counterpartyBankName;
    private String status;
}
