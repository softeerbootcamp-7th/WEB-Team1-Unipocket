package com.genesis.unipocket.travel.query.presentation.response;

public record TravelImageViewUrlResponse(String imageKey, String presignedUrl, int expiresIn) {}
