package za.co.entelect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Transaction")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transactionID")
    private Long transactionID;

    @ManyToOne
    @JoinColumn(name = "accountID", nullable = false)
    private AccountEntity account;

    @Column(name = "transactionReference", unique = true, nullable = false, length = 50)
    private String transactionReference;

    @Column(name = "transactionType", nullable = false, length = 50)
    private String transactionType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "transactionDate")
    private LocalDateTime transactionDate;

    @Column(name = "transactionDescription", nullable = false, length = 255)
    private String transactionDescription;

    @Column(name = "processingBank", nullable = false, length = 255)
    private String processingBank;

    @Column(name = "counterpartyBankName", nullable = false, length = 255)
    private String counterpartyBankName;
}


