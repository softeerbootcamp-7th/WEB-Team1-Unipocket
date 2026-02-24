package com.genesis.unipocket.tempexpense.command.persistence.entity;

import com.genesis.unipocket.tempexpense.common.util.TempExpenseFileNameResolver;
import jakarta.persistence.*;
import lombok.*;

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

	@Column(name = "file_name")
	private String fileName;

	public String getFileName() {
		String resolvedFileName = TempExpenseFileNameResolver.resolveOrFallback(fileName, s3Key);
		if (!resolvedFileName.equals(fileName)) {
			fileName = resolvedFileName;
		}
		return resolvedFileName;
	}

	public enum FileType {
		IMAGE,
		CSV,
		EXCEL
	}
}
