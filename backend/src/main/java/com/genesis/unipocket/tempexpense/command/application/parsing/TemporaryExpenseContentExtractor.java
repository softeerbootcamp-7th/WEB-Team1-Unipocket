package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.storage.s3.S3Service;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemporaryExpenseContentExtractor {

	private final S3Service s3Service;

	public String extractContent(File file) {
		byte[] fileBytes = s3Service.downloadFile(file.getS3Key());
		if (file.getFileType() == File.FileType.CSV) {
			return new String(fileBytes, StandardCharsets.UTF_8);
		}
		if (file.getFileType() == File.FileType.EXCEL) {
			return extractExcelContent(fileBytes);
		}
		throw new BusinessException(ErrorCode.TEMP_EXPENSE_INVALID_FILE_TYPE);
	}

	private String extractExcelContent(byte[] fileBytes) {
		try (InputStream is = new ByteArrayInputStream(fileBytes);
				Workbook workbook = WorkbookFactory.create(is)) {
			StringBuilder sb = new StringBuilder();
			Sheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			for (Row row : sheet) {
				List<String> cells = new ArrayList<>();
				int lastColumn = row.getLastCellNum();
				for (int cn = 0; cn < lastColumn; cn++) {
					Cell cell = row.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
					cells.add(getCellValue(cell, evaluator));
				}
				sb.append(String.join(",", cells)).append("\n");
			}
			return sb.toString();
		} catch (IOException e) {
			log.error("Excel parsing failed", e);
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FAILED);
		}
	}

	private String getCellValue(Cell cell, FormulaEvaluator evaluator) {
		if (cell == null) {
			return "";
		}
		return switch (cell.getCellType()) {
			case STRING -> cell.getStringCellValue();
			case NUMERIC ->
					DateUtil.isCellDateFormatted(cell)
							? cell.getLocalDateTimeCellValue().toString()
							: String.valueOf(cell.getNumericCellValue());
			case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
			case FORMULA -> getFormulaCellValue(cell, evaluator);
			default -> "";
		};
	}

	private String getFormulaCellValue(Cell cell, FormulaEvaluator evaluator) {
		try {
			CellValue evaluated = evaluator.evaluate(cell);
			if (evaluated == null) {
				return "";
			}
			return switch (evaluated.getCellType()) {
				case STRING -> evaluated.getStringValue();
				case NUMERIC ->
						DateUtil.isCellDateFormatted(cell)
								? DateUtil.getLocalDateTime(evaluated.getNumberValue()).toString()
								: String.valueOf(evaluated.getNumberValue());
				case BOOLEAN -> String.valueOf(evaluated.getBooleanValue());
				case BLANK -> "";
				case ERROR -> {
					log.warn(
							"Formula evaluated to error for cell {}: {}",
							cell.getAddress(),
							evaluated.getErrorValue());
					yield "";
				}
				default -> "";
			};
		} catch (Exception e) {
			log.warn("Failed to evaluate formula for cell {}", cell.getAddress(), e);
			return "";
		}
	}
}
