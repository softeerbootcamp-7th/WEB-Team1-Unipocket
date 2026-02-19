export interface Card {
  userCardId: number;
  nickName: string;
  cardNumber: string;
  cardCompany: string;
}

export interface CreateCardRequest {
  nickName: string;
  cardNumber: string;
  cardCompany: string;
}

export interface UpdateCardNicknameRequest {
  nickName: string;
}
