package za.co.entelect.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MakeTransaction {
    private String payFromAccountNumber;
    private String payToAccountNumber;
    private String processingBank;
    private String counterpartyBank;
    private String description;
    private BigDecimal amount;
}
