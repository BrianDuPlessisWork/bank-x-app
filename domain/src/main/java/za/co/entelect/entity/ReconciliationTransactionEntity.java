package za.co.entelect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ReconciliationTransaction")
public class ReconciliationTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reconciliationTransactionID")
    private Long reconciliationTransactionID;

    @ManyToOne
    @JoinColumn(name = "reconciliationBatchID", nullable = false)
    private ReconciliationBatchEntity reconciliationBatch;

    @Column(name = "accountNumber", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "accountType", nullable = false, length = 50)
    private String accountType;

    @Column(name = "branchCode", nullable = false, length = 50)
    private String branchCode;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "transactionReference", nullable = false, length = 50)
    private String transactionReference;

    @Column(name = "transactionType", nullable = false, length = 50)
    private String transactionType;

    @Column(name = "transactionDate", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "counterpartyBankName", nullable = false, length = 255)
    private String counterpartyBankName;

    @Column(name = "status", nullable = false, length = 50)
    private String status;
}

