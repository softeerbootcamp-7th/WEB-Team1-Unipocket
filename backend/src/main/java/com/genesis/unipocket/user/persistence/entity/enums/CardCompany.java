package com.genesis.unipocket.user.persistence.entity.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CardCompany {
	SHINHAN("신한카드", "https://via.placeholder.com/150?text=Shinhan"),
	SAMSUNG("삼성카드", "https://via.placeholder.com/150?text=Samsung"),
	KB("KB국민카드", "https://via.placeholder.com/150?text=KB"),
	HYUNDAI("현대카드", "https://via.placeholder.com/150?text=Hyundai"),
	LOTTE("롯데카드", "https://via.placeholder.com/150?text=Lotte"),
	WOORI("우리카드", "https://via.placeholder.com/150?text=Woori"),
	HANA("하나카드", "https://via.placeholder.com/150?text=Hana"),
	NH("NH농협카드", "https://via.placeholder.com/150?text=NH"),
	BC("BC카드", "https://via.placeholder.com/150?text=BC"),
	IBK("IBK기업은행", "https://via.placeholder.com/150?text=IBK"),
	KAKAO("카카오뱅크", "https://via.placeholder.com/150?text=Kakao"),
	TOSS("토스뱅크", "https://via.placeholder.com/150?text=Toss");

	private final String koreanName;
	private final String imageUrl;
}
