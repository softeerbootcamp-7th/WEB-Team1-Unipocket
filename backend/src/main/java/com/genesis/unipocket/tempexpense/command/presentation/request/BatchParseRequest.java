package com.genesis.unipocket.tempexpense.command.presentation.request;

import java.util.List;

/**
 * <b>Batch 파싱 요청 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record BatchParseRequest(List<String> s3Keys) {}
