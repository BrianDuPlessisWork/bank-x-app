package za.co.entelect.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "ReconciliationBatch")
public class ReconciliationBatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reconciliationBatchID")
    private Long reconciliationBatchID;

    @ManyToOne
    @JoinColumn(name = "processorID", nullable = false)
    private TransactionProcessorEntity transactionProcessor;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "totalTransactions", nullable = false)
    private Integer totalTransactions;

    @Column(name = "submissionDate", nullable = false)
    private LocalDate submissionDate;
}
