package com.genesis.unipocket.user.command.facade.port;

import com.genesis.unipocket.user.command.facade.port.dto.SocialAuthCreate;
import com.genesis.unipocket.user.command.facade.port.dto.SocialAuthInfo;
import java.util.UUID;

public interface SocialAuthWritePort {

	SocialAuthInfo save(SocialAuthCreate command);

	void deleteByUserId(UUID userId);
}
