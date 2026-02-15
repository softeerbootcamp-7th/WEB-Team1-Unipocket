package com.genesis.unipocket.media.command.application;

import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import java.util.List;

public interface MediaObjectStorage {

	String provideGetUrl(String mediaKey);

	PresignedUrlResult getPresignedUrl(String prefix, String originalFileName);

	List<String> listAllKeys();

	void deleteObjects(List<String> keys);
}
