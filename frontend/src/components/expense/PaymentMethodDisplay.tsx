import type { PaymentMethod } from '@/api/expenses/type';
import { CARDS } from '@/data/card/cardCode';

interface PaymentMethodDisplayProps {
  paymentMethod: PaymentMethod;
}

const PaymentMethodDisplay = ({ paymentMethod }: PaymentMethodDisplayProps) => {
  // 현금인 경우
  if (paymentMethod.isCash) {
    return <span className="label1-normal-medium text-label-normal">현금</span>;
  }

  // 카드 정보가 없는 경우
  if (!paymentMethod.card) {
    return <span className="label1-normal-medium text-label-normal">-</span>;
  }

  // 카드인 경우
  const { company, label, lastDigits } = paymentMethod.card;
  const cardInfo = CARDS[company];
  const Logo = cardInfo?.logo;

  return (
    <div
      className="flex items-center gap-1.5"
      title={`${label} ${lastDigits}`} // 마우스 오버 시 전체 정보 표시
    >
      {Logo && <img src={Logo} alt="카드 로고" className="size-5 shrink-0" />}

      <span className="label1-normal-medium text-label-normal truncate">
        {label}
      </span>

      <span className="label1-normal-medium text-label-normal shrink-0">
        {lastDigits}
      </span>
    </div>
  );
};

export default PaymentMethodDisplay;
