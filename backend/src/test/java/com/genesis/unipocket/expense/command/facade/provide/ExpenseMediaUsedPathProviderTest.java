package com.genesis.unipocket.expense.command.facade.provide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.expense.command.persistence.repository.FileRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseMediaUsedPathProviderTest {

	@Mock private FileRepository fileRepository;

	@InjectMocks private ExpenseMediaUsedPathProvider provider;

	@Test
	@DisplayName("expense의 s3Key를 Set으로 반환한다")
	void getUsedPaths_returnsSet() {
		when(fileRepository.findAllS3Keys()).thenReturn(List.of("k1", "k2", "k1"));

		Set<String> result = provider.getUsedPaths();

		assertThat(result).containsExactlyInAnyOrder("k1", "k2");
	}
}
