import type { CardId } from '@/data/card/cardCode';

export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED' | 'BANNED';

export interface User {
  userId: string;
  email: string;
  name: string;
  profileImgUrl: string;
  role: UserRole;
  status: UserStatus;
  needsOnboarding: boolean;
}

export interface Card {
  userCardId: number;
  nickName: string;
  cardNumber: string;
  cardCompany: CardId;
}

export type GetUserResponse = User;

export type GetCardsResponse = Card[];

export type GetCardCompaniesResponse = CardId[];

export type CreateCardRequest = Pick<
  Card,
  'nickName' | 'cardNumber' | 'cardCompany'
>;
