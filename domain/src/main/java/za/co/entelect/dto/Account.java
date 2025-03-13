package za.co.entelect.dto;

import lombok.Builder;
import lombok.Data;
import za.co.entelect.entity.TransactionEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Account {
    private Long accountID;
    private String accountNumber;
    private String accountType;
    private String branchCode;
    private BigDecimal balance;
    private AccountCustomer customer;
}
