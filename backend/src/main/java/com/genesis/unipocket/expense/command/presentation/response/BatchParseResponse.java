package com.genesis.unipocket.expense.command.presentation.response;

/**
 * <b>Batch 파싱 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record BatchParseResponse(String taskId, int totalFiles, String statusUrl) {}
