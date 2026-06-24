package com.matvey.objectaccountingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract", uniqueConstraints = {@UniqueConstraint(columnNames = "number"), @UniqueConstraint(columnNames = "object_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conclusion_date", nullable = false)
    private LocalDate conclusionDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "file_unique_name", nullable = false)
    private String fileUniqueName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id", nullable = false)
    private Object object;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
