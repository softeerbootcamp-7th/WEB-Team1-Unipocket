package com.genesis.unipocket.global.infrastructure.storage.s3.provide;

import com.genesis.unipocket.global.infrastructure.storage.s3.S3Service;
import com.genesis.unipocket.media.command.facade.port.MediaObjectStoragePort;
import java.util.List;

import com.genesis.unipocket.media.command.facade.port.dto.PresignedUrlInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3MediaObjectStorageProvider implements MediaObjectStoragePort {

	private final S3Service s3Service;


	@Override
	public String provideGetUrl(String mediaKey) {
		return s3Service.provideViewUrl(mediaKey);
	}

	@Override
	public PresignedUrlInfo getPresignedUrl(String prefix, String originalFileName) {
		return s3Service.getPresignedUrl(prefix, originalFileName);
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
