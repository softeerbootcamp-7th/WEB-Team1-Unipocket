import CurrencyConverter from '@/components/currency/CurrencyConverter';

interface MoneyContainerProps {
  rateUpdatedAt?: Date;
}

const MoneyContainer = ({ rateUpdatedAt }: MoneyContainerProps) => {
  return (
    <div className="flex w-90 flex-col gap-6">
      <div className="flex flex-col gap-1.5">
        <p className="headline2-bold text-label-normal h-8">금액</p>
        <p className="body2-normal-medium text-label-alternative whitespace-pre-line">
          {'현지 금액이나 기준 금액 중 하나만 입력하면\n자동으로 환산돼요.'}
        </p>
      </div>

      <CurrencyConverter showCurrencyDropdown rateUpdatedAt={rateUpdatedAt} />
    </div>
  );
};

export default MoneyContainer;
