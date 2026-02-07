package com.genesis.unipocket.user.command.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <b>사용자 보유 카드 Entity</b>
 */
@Entity
@Table(name = "user_cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCardEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_card_id")
	private Long userCardId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "nick_name", length = 50)
	private String nickName;

	@Column(name = "card_number", nullable = false, length = 4)
	private String cardNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "card_company", nullable = false, length = 20)
	private com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany cardCompany;

	@Builder
	public UserCardEntity(
			UserEntity user,
			String nickName,
			String cardNumber,
			com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany cardCompany) {
		this.user = user;
		this.nickName = nickName;
		this.cardNumber = cardNumber;
		this.cardCompany = cardCompany;
	}
}
