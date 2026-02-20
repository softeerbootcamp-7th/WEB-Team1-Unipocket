package com.genesis.unipocket.tempexpense.command.presentation.request;

import java.util.List;

public record BatchParseRequest(Long tempExpenseMetaId, List<String> s3Keys) {}
