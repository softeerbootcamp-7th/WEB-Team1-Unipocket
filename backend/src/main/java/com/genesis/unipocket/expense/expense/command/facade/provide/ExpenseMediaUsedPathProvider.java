package com.genesis.unipocket.expense.expense.command.facade.provide;

import com.genesis.unipocket.expense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.media.command.facade.port.MediaUsedPathProvider;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseMediaUsedPathProvider implements MediaUsedPathProvider {

	private final FileRepository fileRepository;

	@Override
	public Set<String> getUsedPaths() {
		return new HashSet<>(fileRepository.findAllS3Keys());
	}
}
