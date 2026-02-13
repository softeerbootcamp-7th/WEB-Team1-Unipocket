package com.genesis.unipocket.travel.command.presentation.request;

import com.genesis.unipocket.global.common.enums.WidgetType;
import jakarta.validation.constraints.NotNull;

public record WidgetRequest(@NotNull WidgetType type, @NotNull Integer order) {}
