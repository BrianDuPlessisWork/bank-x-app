package za.co.entelect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Customer")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerID")
    private Long customerID;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "IdentificationNumber", unique = true, nullable = false)
    private String identificationNumber;

    @Column(name = "emailAddress", unique = true, nullable = false)
    private String emailAddress;

    @Column(name = "cellphoneNumber", unique = true, nullable = false)
    private String cellphoneNumber;
}
