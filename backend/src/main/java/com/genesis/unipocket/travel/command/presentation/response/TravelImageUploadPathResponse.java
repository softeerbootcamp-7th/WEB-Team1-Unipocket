package com.genesis.unipocket.travel.command.presentation.response;

import com.genesis.unipocket.travel.command.facade.port.dto.TravelImageUploadPathInfo;

public record TravelImageUploadPathResponse(String presignedUrl, String imageKey, int expiresIn) {

	public static TravelImageUploadPathResponse from(
			TravelImageUploadPathInfo info, int expiresIn) {
		return new TravelImageUploadPathResponse(info.presignedUrl(), info.imageKey(), expiresIn);
	}
}
