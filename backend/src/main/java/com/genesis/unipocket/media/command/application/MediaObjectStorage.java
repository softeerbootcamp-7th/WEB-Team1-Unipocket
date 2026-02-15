package com.genesis.unipocket.media.command.application;

import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import java.time.Duration;
import java.util.List;

public interface MediaObjectStorage {

	String provideGetUrl(String mediaKey);

	String getPresignedGetUrl(String mediaKey, Duration expiration);

	PresignedUrlResult getPresignedUrl(String prefix, String originalFileName);

	byte[] download(String mediaKey);

	List<String> listAllKeys();

	void deleteObjects(List<String> keys);
}
