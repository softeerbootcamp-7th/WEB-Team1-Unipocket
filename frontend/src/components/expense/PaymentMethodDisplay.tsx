import type { PaymentMethod } from '@/api/expenses/type';
import { CARDS } from '@/data/card/cardCode';

interface PaymentMethodDisplayProps {
  paymentMethod: PaymentMethod;
}

const PaymentMethodDisplay = ({ paymentMethod }: PaymentMethodDisplayProps) => {
  if (paymentMethod.isCash) {
    return <span className="label1-normal-medium text-label-normal">현금</span>;
  }

  if (!paymentMethod.card) {
    return <span className="label1-normal-medium text-label-normal">-</span>;
  }

  const { company, label, lastDigits } = paymentMethod.card;
  // company가 null이나 undefined일 경우를 안전하게 처리
  const cardInfo = company ? CARDS[company] : undefined;
  const Logo = cardInfo?.logo;

  return (
    <div className="flex items-center gap-1.5" title={`${label} ${lastDigits}`}>
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
