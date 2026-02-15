package com.genesis.unipocket.widget.common.validate;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserAccountBookValidatorImpl implements UserAccountBookValidator {
	@Override
	public void validateUserAccountBook(UUID userId, Long accountBookId) {}
}
