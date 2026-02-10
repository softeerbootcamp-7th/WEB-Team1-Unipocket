package com.genesis.unipocket.travel.query.persistence.response;

import com.genesis.unipocket.travel.common.enums.WidgetType;

public record WidgetOrderDto(WidgetType type, Integer order) {}
