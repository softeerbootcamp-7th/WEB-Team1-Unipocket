import PaymentMethodDisplay from '@/components/expense/PaymentMethodDisplay';

import { useGetCardsQuery } from '@/api/users/query';

const TempPaymentMethodCell = ({
  cardLastFourDigits,
}: {
  cardLastFourDigits: string;
}) => {
  const { data: cards = [] } = useGetCardsQuery();

  const isCash = !cardLastFourDigits;

  if (isCash) {
    return (
      <PaymentMethodDisplay paymentMethod={{ isCash: true, card: null }} />
    );
  }

  const matchedCard = cards.find((c) => c.cardNumber === cardLastFourDigits);

  const mockPayment = {
    isCash: false,
    card: matchedCard
      ? {
          userCardId: matchedCard.userCardId,
          company: matchedCard.cardCompany,
          label: matchedCard.nickName,
          lastDigits: matchedCard.cardNumber,
        }
      : {
          userCardId: 0,
          company: null,
          label: '미확인 카드',
          lastDigits: cardLastFourDigits,
        },
  };

  return <PaymentMethodDisplay paymentMethod={mockPayment} />;
};

export default TempPaymentMethodCell;
