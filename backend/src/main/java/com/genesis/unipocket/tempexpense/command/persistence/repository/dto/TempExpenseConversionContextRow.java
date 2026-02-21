package com.genesis.unipocket.tempexpense.command.persistence.repository.dto;

import com.genesis.unipocket.tempexpense.command.persistence.entity.File;

public record TempExpenseConversionContextRow(
		Long accountBookId, File.FileType fileType, String s3Key) {}
