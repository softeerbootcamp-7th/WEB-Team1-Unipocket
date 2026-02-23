import type { Expense } from '@/api/expenses/type';
import type { Card } from '@/api/users/type';
import { CASH } from '@/constants/column';

const getCardNumberFromExpense = (original: Expense, cards: Card[]): string => {
  if (!original?.paymentMethod || original.paymentMethod.isCash) return CASH;
  if (original.cardNumber) return original.cardNumber;
  if (original.paymentMethod.card) {
    const { lastDigits } = original.paymentMethod.card;
    const matchedCard = cards.find((c) => c.cardNumber.endsWith(lastDigits));
    if (matchedCard) return matchedCard.cardNumber;
  }
  return CASH;
};

const resolveUserCardId = (
  cardNumber: string,
  cards: Card[],
): number | null => {
  if (cardNumber === CASH) return null;
  const card = cards.find((c) => c.cardNumber === cardNumber);
  return card && 'userCardId' in card ? card.userCardId : null;
};

export { getCardNumberFromExpense, resolveUserCardId };
