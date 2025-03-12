package za.co.entelect.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Account")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accountID")
    private Long accountID;

    @Column(name = "accountNumber", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "accountType", nullable = false)
    private String accountType;

    @Column(name = "branchCode", nullable = false)
    private String branchCode;

    @Column(name = "balance", nullable = false)
    private Double balance;

    @ManyToOne
    @JoinColumn(name = "customerID", nullable = false)
    private CustomerEntity customer;
}
