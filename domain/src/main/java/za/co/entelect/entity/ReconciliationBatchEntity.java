package za.co.entelect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ReconciliationBatch")
public class ReconciliationBatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reconciliationBatchID")
    private Long reconciliationBatchID;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "submissionDate", nullable = false)
    private LocalDateTime submissionDate;

    @Column(name = "processingBank", nullable = false)
    private String processingBank;
}
