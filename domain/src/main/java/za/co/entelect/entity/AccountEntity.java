package za.co.entelect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
    private BigDecimal balance;

    @ManyToOne
    @JoinColumn(name = "customerID", nullable = false)
    private CustomerEntity customer;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntity> transactions;
}
