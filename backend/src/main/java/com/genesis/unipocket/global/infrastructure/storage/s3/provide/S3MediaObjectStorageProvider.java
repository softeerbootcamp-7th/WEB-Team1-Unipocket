package com.genesis.unipocket.global.infrastructure.storage.s3.provide;

import com.genesis.unipocket.global.infrastructure.storage.s3.S3Service;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3MediaObjectStorageProvider implements MediaObjectStorage {

	private final S3Service s3Service;

	@Override
	public String provideGetUrl(String mediaKey) {
		return s3Service.provideViewUrl(mediaKey);
	}

	@Override
	public String getPresignedGetUrl(String mediaKey, Duration expiration) {
		return s3Service.getPresignedGetUrl(mediaKey, expiration);
	}

	@Override
	public PresignedUrlResult getPresignedUrl(String prefix, String originalFileName) {
		return s3Service.getPresignedUrl(prefix, originalFileName);
	}

	@Override
	public byte[] download(String mediaKey) {
		return s3Service.downloadFile(mediaKey);
	}

	@Override
	public List<String> listAllKeys() {
		return s3Service.listAllObjectKeys();
	}

	@Override
	public void deleteObjects(List<String> keys) {
		s3Service.deleteObjects(keys);
	}
}
