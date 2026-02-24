package com.genesis.unipocket.tempexpense.command.application.parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.tempexpense.command.facade.port.TempExpenseMediaAccessService;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemporaryExpenseContentExtractorTest {

	@Mock private TempExpenseMediaAccessService tempExpenseMediaAccessService;

	@Test
	@DisplayName("CSV는 CP949 인코딩이어도 UTF-8 호환 텍스트로 정규화한다")
	void extractContent_csv_cp949DecodesSuccessfully() {
		TemporaryExpenseContentExtractor extractor =
				new TemporaryExpenseContentExtractor(tempExpenseMediaAccessService);
		String s3Key = "temp/a.csv";
		String rawCsv = "거래일,가맹점명,금액\n" + "2025.08.31 21:01,NAME-CHEAP.COM,\"10,620\"\n";

		when(tempExpenseMediaAccessService.download(s3Key))
				.thenReturn(rawCsv.getBytes(Charset.forName("MS949")));

		File file = File.builder().fileType(File.FileType.CSV).s3Key(s3Key).build();

		String normalized = extractor.extractContent(file);

		assertThat(normalized).startsWith("거래일,가맹점명,금액\n");
		assertThat(normalized).contains("2025.08.31 21:01,NAME-CHEAP.COM,\"10,620\"");
	}

	@Test
	@DisplayName("Excel은 canonical CSV 텍스트로 정규화한다")
	void extractContent_excel_convertsToCanonicalCsv() throws Exception {
		TemporaryExpenseContentExtractor extractor =
				new TemporaryExpenseContentExtractor(tempExpenseMediaAccessService);
		String s3Key = "temp/a.xlsx";

		byte[] excelBytes;
		try (XSSFWorkbook workbook = new XSSFWorkbook();
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Sheet1");
			Row header = sheet.createRow(0);
			header.createCell(0).setCellValue("거래일");
			header.createCell(1).setCellValue("가맹점명");
			header.createCell(2).setCellValue("금액");

			Row row = sheet.createRow(1);
			row.createCell(0).setCellValue("2025.08.31 21:01");
			row.createCell(1).setCellValue("NAME, CHEAP");
			row.createCell(2).setCellValue("10,620");

			workbook.write(out);
			excelBytes = out.toByteArray();
		}

		when(tempExpenseMediaAccessService.download(s3Key)).thenReturn(excelBytes);

		File file = File.builder().fileType(File.FileType.EXCEL).s3Key(s3Key).build();

		String normalized = extractor.extractContent(file);

		assertThat(normalized).startsWith("거래일,가맹점명,금액\n");
		assertThat(normalized).contains("2025.08.31 21:01,\"NAME, CHEAP\",\"10,620\"");
	}
}
