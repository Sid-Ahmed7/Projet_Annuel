package com.glotrush.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.glotrush.enumerations.PaymentInterval;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentInterval paymentInterval;

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_date" , nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_date" , nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    
    
}
