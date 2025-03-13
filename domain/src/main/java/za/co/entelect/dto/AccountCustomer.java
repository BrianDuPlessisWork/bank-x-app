package za.co.entelect.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountCustomer {
    private Long customerID;
    private String name;
    private String surname;
    private String emailAddress;
}
