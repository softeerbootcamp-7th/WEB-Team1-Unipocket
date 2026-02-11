package com.genesis.unipocket.media.command.facade.port;

import com.genesis.unipocket.media.command.facade.port.dto.PresignedUrlInfo;

import java.util.List;

public interface MediaObjectStoragePort {

	String provideGetUrl(String mediaKey);

	PresignedUrlInfo getPresignedUrl(String prefix, String originalFileName);

	List<String> listAllKeys();

	void deleteObjects(List<String> keys);
}
