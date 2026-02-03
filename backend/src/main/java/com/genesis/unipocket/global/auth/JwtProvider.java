package com.genesis.unipocket.global.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

	private final SecretKey key;

	// 생성자에서 키를 미리 만들어둡니다. 0.12.x는 SecretKey 객체를 권장합니다.
	public JwtProvider(@Value("${jwt.secret}") String secretKey) {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 토큰 생성 공통 로직 (0.12.3 버전 문법)
	 */
	public String createToken(Long userId, long validityMs) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + validityMs);

		return Jwts.builder()
				.subject(String.valueOf(userId)) // setSubject -> subject
				.issuedAt(now) // setIssuedAt -> issuedAt
				.expiration(expiryDate) // setExpiration -> expiration
				.signWith(key) // 알고리즘 자동 선택 (HS256)
				.compact();
	}
}
