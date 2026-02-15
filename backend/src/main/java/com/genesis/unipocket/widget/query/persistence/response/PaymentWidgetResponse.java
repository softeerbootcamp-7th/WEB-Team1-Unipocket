package com.genesis.unipocket.widget.query.persistence.response;

import java.util.List;

public record PaymentWidgetResponse(int paymentMethodCount, List<PaymentItem> items) {

	public record PaymentItem(String name, int percent) {}
}
