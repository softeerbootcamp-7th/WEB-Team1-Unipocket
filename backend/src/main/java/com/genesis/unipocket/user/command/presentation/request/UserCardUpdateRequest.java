package com.genesis.unipocket.user.command.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCardUpdateRequest(@NotBlank @Size(min = 2, max = 50) String nickname) {}
