import PaymentMethodDisplay from '@/components/expense/PaymentMethodDisplay';

import { useGetCardsQuery } from '@/api/users/query';

interface TempPaymentMethodCellProps {
  cardLastFourDigits: string | null;
}

const TempPaymentMethodCell = ({
  cardLastFourDigits,
}: TempPaymentMethodCellProps) => {
  const { data: cards = [] } = useGetCardsQuery();

  // 3. 만약 cardLastFourDigits이 null(또는 빈 문자열)이면 현금 선택되도록 하기
  if (!cardLastFourDigits || cardLastFourDigits.trim() === '') {
    return (
      <PaymentMethodDisplay paymentMethod={{ isCash: true, card: null }} />
    );
  }

  // 1. 등록된 카드 중에 같은 cardNumber가 있는 카드 찾기
  const matchedCard = cards.find((c) => c.cardNumber === cardLastFourDigits);

  const mockPayment = {
    isCash: false,
    card: matchedCard
      ? {
          // 일치하는 카드가 있는 경우
          userCardId: matchedCard.userCardId,
          company: matchedCard.cardCompany,
          label: matchedCard.nickName,
          lastDigits: matchedCard.cardNumber,
        }
      : {
          // 2. 같은 카드가 없으면 미확인 + cardLastFourDigits 보여주기
          userCardId: 0, // 미확인 카드를 위한 임시 ID
          company: null,
          label: '미확인 카드',
          lastDigits: cardLastFourDigits,
        },
  };

  return <PaymentMethodDisplay paymentMethod={mockPayment} />;
};

export default TempPaymentMethodCell;
