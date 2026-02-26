package com.genesis.unipocket.tempexpense.command.presentation.request;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * record 대신 일반 클래스를 사용하는 이유:
 * jackson-datatype-jdk8 2.13+에서 record의 Optional 필드가 JSON에 없을 때
 * getAbsentValue()가 호출되어 Optional.empty()를 반환한다.
 * 일반 클래스는 absent 필드에 setter를 호출하지 않으므로 Java 기본값인 null이 유지된다.
 * null = 필드 미전송(기존값 유지), Optional.empty() = 명시적 null(초기화) 시맨틱을 보장하기 위해 POJO를 사용한다.
 */
@Getter
@Setter
@NoArgsConstructor
public class TemporaryExpenseMetaBulkUpdateItemRequest {

	@NotNull private Long tempExpenseId;

	private String merchantName;

	private Category category;

	private CurrencyCode localCountryCode;

	private BigDecimal localCurrencyAmount;

	private CurrencyCode baseCountryCode;

	private BigDecimal baseCurrencyAmount;

	private String memo;

	private LocalDateTime occurredAt;

	private Optional<@Pattern(regexp = "\\d{4}", message = "카드 번호 뒷자리는 4자리 숫자여야 합니다.") String>
			cardLastFourDigits;
}
