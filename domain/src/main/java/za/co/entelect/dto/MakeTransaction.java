package za.co.entelect.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MakeTransaction {
    private String payFromAccountNumber;
    private String payToAccountNumber;
    private String processingBank;
    private String counterpartyBank;
    private String description;
    private BigDecimal amount;
}
