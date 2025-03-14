package za.co.entelect.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Transaction {
    private Long transactionID;
    private TransactionAccount account;
    private BigDecimal amount;
    private String transactionReference;
    private String transactionType;
    private String transactionDescription;
    private LocalDateTime transactionDate;
    private String counterpartyBankName;
    private String processingBank;
}
