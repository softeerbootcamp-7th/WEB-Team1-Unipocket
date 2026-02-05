package com.genesis.unipocket.travel.domain;

import com.genesis.unipocket.global.base.BaseEntity;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel")
public class Travel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_id")
    private Long id;

    @Column(nullable = false)
    private Long accountBookId;

    @Column(name = "image_key")
    private String imageKey;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String travelPlaceName;

    public void validateDateRange() {
        if (startDate.isAfter(endDate)) {
            // Need to define INVALID_DATE_RANGE in ErrorCode first or use generic invalid
            // argument
            throw new IllegalArgumentException("Travel end date must be after start date");
        }
    }
}
