package com.genesis.unipocket.tempexpense.command.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@Table(name = "temp_expense_meta")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TempExpenseMeta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "temp_expense_meta_id")
	private Long tempExpenseMetaId;

	@Column(name = "account_book_id", nullable = false)
	private Long accountBookId;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
