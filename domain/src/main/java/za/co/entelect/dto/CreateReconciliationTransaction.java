package za.co.entelect.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CreateReconciliationTransaction {
    private String accountNumber;
    private String accountType;
    private String branchCode;
    private BigDecimal amount;
    private String transactionReference;
    private String transactionType;
    private LocalDateTime transactionDate;
    private String counterpartyBankName;
}
