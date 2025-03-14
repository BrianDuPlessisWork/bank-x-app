package za.co.entelect.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransactionAccount {
    private Long accountID;
    private String accountNumber;
    private String accountType;
    private String branchCode;
}
