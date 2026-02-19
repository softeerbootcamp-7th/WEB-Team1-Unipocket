package com.genesis.unipocket.accountbook.command.application.port.dto;

import java.util.UUID;

public record AccountBookUserInfo(UUID userId, boolean hasMainBucket) {}
