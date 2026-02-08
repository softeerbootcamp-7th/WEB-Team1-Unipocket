package com.genesis.unipocket.expense.persistence.entity.expense;

import jakarta.persistence.*;
import lombok.*;

/**
 * <b>파일 엔티티</b>
 * <p>
 * S3에 업로드된 파일 정보를 저장합니다.
 * </p>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Entity
@Getter
@Builder
@Table(name = "file")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "file_id")
	private Long fileId;

	@Column(name = "temp_expense_meta_id", nullable = false)
	private Long tempExpenseMetaId;

	@Enumerated(EnumType.STRING)
	@Column(name = "file_type", nullable = false)
	private FileType fileType;

	@Column(name = "s3_key", nullable = false)
	private String s3Key;

	/**
	 * 파일 타입 Enum
	 */
	public enum FileType {
		IMAGE, // 이미지 파일 (jpg, png 등)
		CSV // CSV 파일
	}
}
