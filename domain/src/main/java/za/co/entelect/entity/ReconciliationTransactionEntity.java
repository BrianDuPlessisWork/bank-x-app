package za.co.entelect.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

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

    @Column(name = "accountNumber", nullable = false)
    private String accountNumber;

    @Column(name = "accountType", nullable = false)
    private String accountType;

    @Column(name = "branchCode", nullable = false)
    private String branchCode;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "transactionType", nullable = false)
    private String transactionType;

    @Column(name = "transactionDate", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "counterPartyAccountNumber", nullable = false)
    private String counterPartyAccountNumber;

    @Column(name = "counterPartyAccountType", nullable = false)
    private String counterPartyAccountType;

    @Column(name = "counterPartyBranchCode", nullable = false)
    private String counterPartyBranchCode;

    @Column(name = "status", nullable = false)
    private String status;

}
