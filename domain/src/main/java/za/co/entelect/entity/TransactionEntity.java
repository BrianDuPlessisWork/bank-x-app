package za.co.entelect.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

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

    @ManyToOne
    @JoinColumn(name = "processorID", nullable = false)
    private TransactionProcessorEntity transactionProcessor;

    @ManyToOne
    @JoinColumn(name = "counterpartyID", nullable = false)
    private CounterpartyEntity counterparty;

    @Column(name = "transactionType", nullable = false)
    private String transactionType;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "transactionFee", nullable = false)
    private Double transactionFee;

    @Column(name = "transactionDate", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "reference", nullable = false)
    private String reference;
}
