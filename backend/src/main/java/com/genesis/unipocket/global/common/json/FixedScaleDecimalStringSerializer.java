package com.genesis.unipocket.global.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedScaleDecimalStringSerializer extends JsonSerializer<BigDecimal> {

	@Override
	public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}
		gen.writeString(value.setScale(2, RoundingMode.DOWN).toPlainString());
	}
}
