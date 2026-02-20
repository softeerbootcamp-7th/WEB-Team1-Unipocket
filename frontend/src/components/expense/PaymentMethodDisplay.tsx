import type { PaymentMethod } from '@/api/expenses/type';
import { CARDS } from '@/data/card/cardCode';

interface PaymentMethodDisplayProps {
  paymentMethod: PaymentMethod;
}

const PaymentMethodDisplay = ({ paymentMethod }: PaymentMethodDisplayProps) => {
  // 1. 현금인 경우
  if (paymentMethod.isCash) {
    return <span className="label1-normal-medium text-label-normal">현금</span>;
  }

  // 2. 카드 정보가 없는 경우 방어 로직
  if (!paymentMethod.card) {
    return <span className="label1-normal-medium text-label-normal">-</span>;
  }

  // 3. 카드인 경우
  const { company, label, lastDigits } = paymentMethod.card;
  const cardInfo = CARDS[company];
  const Logo = cardInfo?.logo;

  return (
    <div className="flex items-center gap-2">
      {Logo && <Logo className="size-5 shrink-0" />}
      <span className="text-label-normal label1-normal-medium">
        {cardInfo?.code} {label?.slice(0, 2)} {lastDigits?.slice(-3)}
      </span>
    </div>
  );
};

export default PaymentMethodDisplay;
