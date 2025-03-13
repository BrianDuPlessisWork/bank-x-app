package za.co.entelect.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Customer {
    private Long customerID;
    private String name;
    private String surname;
    private String identificationNumber;
    private String emailAddress;
    private String cellphoneNumber;
}
