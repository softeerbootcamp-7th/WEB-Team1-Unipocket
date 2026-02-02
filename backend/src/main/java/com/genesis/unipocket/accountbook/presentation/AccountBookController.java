package com.genesis.unipocket.accountbook.presentation;

import com.genesis.unipocket.accountbook.facade.AccountBookFacade;
import com.genesis.unipocket.accountbook.presentation.dto.request.CreateAccountBookReq;
import com.genesis.unipocket.accountbook.presentation.dto.response.CreateAccountBookRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountBookController {

	private final AccountBookFacade accountBookFacade;

	@PostMapping("/account-books")
	public ResponseEntity<CreateAccountBookRes> createAccountBook(
			@Valid @RequestBody CreateAccountBookReq req) {

		return ResponseEntity.ok(CreateAccountBookRes.from(null));
	}
}
