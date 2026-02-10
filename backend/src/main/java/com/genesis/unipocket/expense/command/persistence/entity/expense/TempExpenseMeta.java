package com.genesis.unipocket.expense.command.persistence.entity.expense;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * <b>임시지출내역 메타데이터 엔티티</b>
 * <p>
 * 파일 업로드 및 파싱 결과에 대한 메타정보를 저장합니다.
 * </p>
 *
 * @author 김동균
 * @since 2026-02-08
 */
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
