package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.facade.port.TempExpenseMediaAccessService;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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

	private static final Charset CP949_CHARSET = Charset.forName("MS949");

	private final TempExpenseMediaAccessService tempExpenseMediaAccessService;

	public String extractContent(File file) {
		byte[] fileBytes = tempExpenseMediaAccessService.download(file.getS3Key());
		if (file.getFileType() == File.FileType.CSV) {
			return extractCsvContent(fileBytes);
		}
		if (file.getFileType() == File.FileType.EXCEL) {
			return extractExcelContent(fileBytes);
		}
		throw new BusinessException(ErrorCode.TEMP_EXPENSE_INVALID_FILE_TYPE);
	}

	private String extractCsvContent(byte[] fileBytes) {
		String decoded = decodeCsv(fileBytes);
		List<List<String>> rows = parseCsvRows(decoded);
		return toCanonicalCsv(rows);
	}

	private String extractExcelContent(byte[] fileBytes) {
		try (InputStream is = new ByteArrayInputStream(fileBytes);
				Workbook workbook = WorkbookFactory.create(is)) {
			List<List<String>> rows = new ArrayList<>();
			Sheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			DataFormatter formatter = new DataFormatter();

			for (Row row : sheet) {
				List<String> cells = new ArrayList<>();
				int lastColumn = row.getLastCellNum();
				if (lastColumn <= 0) {
					continue;
				}
				for (int cn = 0; cn < lastColumn; cn++) {
					Cell cell = row.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
					cells.add(getCellValue(cell, evaluator, formatter));
				}
				rows.add(cells);
			}
			return toCanonicalCsv(rows);
		} catch (IOException e) {
			log.error("Excel parsing failed", e);
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FAILED);
		}
	}

	private String getCellValue(Cell cell, FormulaEvaluator evaluator, DataFormatter formatter) {
		if (cell == null) {
			return "";
		}
		return formatter.formatCellValue(cell, evaluator).trim();
	}

	private String decodeCsv(byte[] fileBytes) {
		if (hasUtf8Bom(fileBytes)) {
			byte[] withoutBom = new byte[fileBytes.length - 3];
			System.arraycopy(fileBytes, 3, withoutBom, 0, withoutBom.length);
			return new String(withoutBom, StandardCharsets.UTF_8);
		}
		try {
			return decodeStrict(fileBytes, StandardCharsets.UTF_8);
		} catch (CharacterCodingException utf8Error) {
			try {
				return decodeStrict(fileBytes, CP949_CHARSET);
			} catch (CharacterCodingException cp949Error) {
				log.warn("CSV charset decode fallback to UTF-8 replacement mode");
				return new String(fileBytes, StandardCharsets.UTF_8);
			}
		}
	}

	private String decodeStrict(byte[] fileBytes, Charset charset) throws CharacterCodingException {
		CharsetDecoder decoder = charset.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		return decoder.decode(ByteBuffer.wrap(fileBytes)).toString();
	}

	private boolean hasUtf8Bom(byte[] fileBytes) {
		return fileBytes.length >= 3
				&& (fileBytes[0] & 0xFF) == 0xEF
				&& (fileBytes[1] & 0xFF) == 0xBB
				&& (fileBytes[2] & 0xFF) == 0xBF;
	}

	private List<List<String>> parseCsvRows(String content) {
		List<List<String>> rows = new ArrayList<>();
		List<String> currentRow = new ArrayList<>();
		StringBuilder currentValue = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < content.length(); i++) {
			char ch = content.charAt(i);

			if (inQuotes) {
				if (ch == '"') {
					if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
						currentValue.append('"');
						i++;
					} else {
						inQuotes = false;
					}
				} else {
					currentValue.append(ch);
				}
				continue;
			}

			if (ch == '"') {
				inQuotes = true;
				continue;
			}
			if (ch == ',') {
				currentRow.add(currentValue.toString());
				currentValue.setLength(0);
				continue;
			}
			if (ch == '\n') {
				currentRow.add(currentValue.toString());
				rows.add(currentRow);
				currentRow = new ArrayList<>();
				currentValue.setLength(0);
				continue;
			}
			if (ch == '\r') {
				continue;
			}
			currentValue.append(ch);
		}

		if (currentValue.length() > 0 || !currentRow.isEmpty()) {
			currentRow.add(currentValue.toString());
			rows.add(currentRow);
		}

		return rows;
	}

	private String toCanonicalCsv(List<List<String>> rows) {
		StringBuilder sb = new StringBuilder();
		for (List<String> row : rows) {
			int lastNonEmptyIndex = findLastNonEmptyIndex(row);
			if (lastNonEmptyIndex < 0) {
				continue;
			}
			for (int i = 0; i <= lastNonEmptyIndex; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(escapeCsv(row.get(i)));
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	private int findLastNonEmptyIndex(List<String> row) {
		for (int i = row.size() - 1; i >= 0; i--) {
			if (!row.get(i).isBlank()) {
				return i;
			}
		}
		return -1;
	}

	private String escapeCsv(String value) {
		String safe = value == null ? "" : value;
		boolean requiresQuoting =
				safe.contains(",")
						|| safe.contains("\"")
						|| safe.contains("\n")
						|| safe.contains("\r");
		if (!requiresQuoting) {
			return safe;
		}
		return "\"" + safe.replace("\"", "\"\"") + "\"";
	}
}
