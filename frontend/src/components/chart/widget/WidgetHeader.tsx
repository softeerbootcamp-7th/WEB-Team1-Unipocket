import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import ExpenseCard from '@/components/home-page/ExpenseCard';

interface WidgetHeaderProps {
  isWidgetEditMode: boolean;
  toggleEditMode: () => void;
}

const WidgetHeader = ({
  isWidgetEditMode,
  toggleEditMode,
}: WidgetHeaderProps) => {
  const ButtonVariant = isWidgetEditMode ? 'solid' : 'outlined';

  return (
    <div className="flex items-end gap-4">
      <ExpenseCard
        label="총 지출"
        baseCountryCode="KR"
        baseCountryAmount={1402432}
        localCountryCode="US"
        localCountryAmount={12232}
      />
      <Divider style="vertical" className="h-15" />
      <ExpenseCard
        label="이번 달 지출"
        baseCountryCode="KR"
        baseCountryAmount={200342}
        localCountryCode="US"
        localCountryAmount={12232}
      />
      <div className="flex-1" />
      <Button variant={ButtonVariant} size="md" onClick={toggleEditMode}>
        {isWidgetEditMode ? '위젯 편집 완료하기' : '위젯 편집하기'}
      </Button>
    </div>
  );
};

export default WidgetHeader;
