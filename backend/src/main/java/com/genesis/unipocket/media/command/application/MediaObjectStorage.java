package com.genesis.unipocket.media.command.application;

import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import java.time.Duration;
import java.util.List;

public interface MediaObjectStorage {

	String provideGetUrl(String mediaKey);

	String getPresignedGetUrl(String mediaKey, Duration expiration);

	boolean exists(String mediaKey);

	PresignedUrlResult getPresignedUrl(String prefix, MediaContentType mediaContentType);

	byte[] download(String mediaKey);

	List<String> listAllKeys();

	void deleteObjects(List<String> keys);
}
