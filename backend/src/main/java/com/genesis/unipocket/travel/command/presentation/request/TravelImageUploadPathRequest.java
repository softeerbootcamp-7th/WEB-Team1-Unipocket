package com.genesis.unipocket.travel.command.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record TravelImageUploadPathRequest(
		@NotBlank(message = "mimeType은 필수입니다.") String mimeType) {}
