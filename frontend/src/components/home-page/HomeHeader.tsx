import { useWidgetContext } from '@/components/chart/widget/WidgetContext';
import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import ExpenseCard from '@/components/home-page/ExpenseCard';

import { useGetAccountBookAmountQuery } from '@/api/account-books/query';
import type { CountryCode } from '@/data/country/countryCode';
import { formatDateStringToDot, formatDateToDot } from '@/lib/utils';

const HomeHeader = () => {
  const { isWidgetEditMode, toggleEditMode } = useWidgetContext();
  const ButtonVariant = isWidgetEditMode ? 'solid' : 'outlined';
  const { data: amountData } = useGetAccountBookAmountQuery();

  const today = new Date();
  const todayDot = formatDateToDot(today);
  const firstDayOfMonthDot = formatDateToDot(
    new Date(today.getFullYear(), today.getMonth(), 1),
  );
  const oldestExpenseDateDot = amountData?.oldestExpenseDate
    ? formatDateStringToDot(amountData.oldestExpenseDate)
    : todayDot;
  const newestExpenseDateDot = amountData?.newestExpenseDate
    ? formatDateStringToDot(amountData.newestExpenseDate)
    : todayDot;

  return (
    <div className="flex items-end gap-4">
      <ExpenseCard
        label="총 지출"
        baseCountryCode={(amountData?.baseCountryCode ?? 'KR') as CountryCode}
        baseCountryAmount={Number(amountData?.totalBaseAmount ?? 0)}
        localCountryCode={(amountData?.localCountryCode ?? 'KR') as CountryCode}
        localCountryAmount={Number(amountData?.totalLocalAmount ?? 0)}
        tooltipContent={`산정 기간 : ${oldestExpenseDateDot} ~ ${newestExpenseDateDot}`}
      />
      <Divider style="vertical" className="h-15" />
      <ExpenseCard
        label="이번 달 지출"
        baseCountryCode={(amountData?.baseCountryCode ?? 'KR') as CountryCode}
        baseCountryAmount={Number(amountData?.thisMonthBaseAmount ?? 0)}
        localCountryCode={(amountData?.localCountryCode ?? 'KR') as CountryCode}
        localCountryAmount={Number(amountData?.thisMonthLocalAmount ?? 0)}
        tooltipContent={`산정 기간 : ${firstDayOfMonthDot} ~ ${todayDot}`}
      />
      <div className="flex-1" />
      <Button variant={ButtonVariant} size="md" onClick={toggleEditMode}>
        {isWidgetEditMode ? '위젯 편집 완료하기' : '위젯 편집하기'}
      </Button>
    </div>
  );
};

export default HomeHeader;
