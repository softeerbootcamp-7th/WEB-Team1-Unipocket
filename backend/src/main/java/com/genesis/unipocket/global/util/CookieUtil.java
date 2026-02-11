package com.genesis.unipocket.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

	@Value("${app.cookie.domain:#{null}}")
	private String cookieDomain;

	@Value("${app.cookie.secure:false}")
	private boolean cookieSecure;

	public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		addCookie(response, name, value, maxAge, "/");
	}

	public void addCookie(
			HttpServletResponse response, String name, String value, int maxAge, String path) {
		String cookiePath = (path == null || path.isBlank()) ? "/" : path;
		ResponseCookie.ResponseCookieBuilder cookieBuilder =
				ResponseCookie.from(name, value)
						.path(cookiePath)
						.maxAge(maxAge)
						.httpOnly(true)
						.sameSite("Lax")
						.secure(cookieSecure);

		if (cookieDomain != null && !cookieDomain.isBlank()) {
			cookieBuilder.domain(cookieDomain);
		}

		response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
	}

	public void deleteCookie(HttpServletResponse response, String name) {
		deleteCookie(response, name, "/");
	}

	public void deleteCookie(HttpServletResponse response, String name, String path) {
		String cookiePath = (path == null || path.isBlank()) ? "/" : path;
		ResponseCookie.ResponseCookieBuilder cookieBuilder =
				ResponseCookie.from(name, "")
						.path(cookiePath)
						.maxAge(0)
						.httpOnly(true)
						.sameSite("Lax")
						.secure(cookieSecure);

		if (cookieDomain != null && !cookieDomain.isBlank()) {
			cookieBuilder.domain(cookieDomain);
		}

		response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
	}
}
