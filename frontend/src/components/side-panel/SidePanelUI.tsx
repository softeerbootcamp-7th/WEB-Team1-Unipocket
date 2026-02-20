import { useLayoutEffect, useRef, useState } from 'react';
import { clsx } from 'clsx';

import { useClickOutside } from '@/hooks/useClickOutside';

import { formatDateTime } from '@/components/calendar/date.utils';
import Button from '@/components/common/Button';
import { CategoryChip } from '@/components/common/Chip';
import Divider from '@/components/common/Divider';
import Icon from '@/components/common/Icon';
import TextInput from '@/components/common/TextInput';
import type { CurrencyValues } from '@/components/currency/CurrencyConverter';
import PaymentMethodDisplay from '@/components/expense/PaymentMethodDisplay';
import DateTimePicker from '@/components/side-panel/DateTimePicker';
import MoneyContainer from '@/components/side-panel/MoneyContainer';
import type { SidePanelFormValues } from '@/components/side-panel/type';
import useSidePanelForm from '@/components/side-panel/useSidePanelForm';
import ValueContainer, {
  type ValueItemProps,
} from '@/components/side-panel/ValueContainer';
import type { UploadEntryType } from '@/components/upload/UploadMenu';

import type { Expense } from '@/api/expenses/type';

const uploadTitleMap: Record<
  Exclude<UploadEntryType, 'manual' | null>,
  string
> = {
  file: '파일',
  image: '사진',
};

interface SidePanelUIProps {
  mode?: UploadEntryType;
  isOpen: boolean;
  onClose: () => void;
  initialData?: Partial<Expense>;
  onSubmit?: (values: SidePanelFormValues) => void;
}

const SidePanelUI = ({
  mode,
  isOpen,
  onClose,
  initialData,
  onSubmit,
}: SidePanelUIProps) => {
  const panelRef = useRef<HTMLDivElement>(null);
  const titleRef = useRef<HTMLTextAreaElement>(null);

  const {
    title,
    setTitle,
    memo,
    setMemo,
    selectedDateTime,
    setSelectedDateTime,
    isDateTimePickerOpen,
    setIsDateTimePickerOpen,
  } = useSidePanelForm(initialData);

  const [currencyValues, setCurrencyValues] = useState<CurrencyValues | null>(
    null,
  );

  const categoryValue = initialData?.category ? (
    <CategoryChip categoryId={initialData.category} />
  ) : (
    '-'
  );

  const paymentValue = initialData?.paymentMethod ? (
    initialData.paymentMethod.isCash ? (
      '현금'
    ) : (
      <PaymentMethodDisplay paymentMethod={initialData.paymentMethod} />
    )
  ) : (
    '-'
  );

  const valueItems = [
    {
      label: '일시',
      value: selectedDateTime ? formatDateTime(selectedDateTime) : '비어 있음',
      onClick: () => setIsDateTimePickerOpen((prev) => !prev),
    },

    { label: '카테고리', value: categoryValue },
    { label: '결제 수단', value: paymentValue },
    { label: '여행', value: initialData?.travel?.name ?? '-' },
  ] as const satisfies ValueItemProps[];

  const handleSubmit = () => {
    if (!onSubmit) return;
    if (!selectedDateTime) return;

    onSubmit({
      merchantName: title,
      category: initialData?.category ?? 1, // @TODO: 실제 선택 상태로 교체
      userCardId: initialData?.paymentMethod?.isCash ? undefined : 1, // @TODO: 실제 선택 상태로 교체
      occurredAt: selectedDateTime,
      localCurrencyAmount: currencyValues?.localAmount ?? 0,
      localCurrencyCode: currencyValues?.localCurrencyCode ?? 'USD',
      baseCurrencyAmount: currencyValues?.baseAmount ?? 0,
      memo,
      travelId: initialData?.travel?.id ?? undefined, // @TODO: 실제 선택 상태로 교체
    });
  };

  useLayoutEffect(() => {
    if (!titleRef.current) return;
    titleRef.current.style.height = '0px';
    titleRef.current.style.height = `${titleRef.current.scrollHeight}px`;
  }, [title]);

  useClickOutside(
    panelRef,
    () => {
      if (!isOpen) return;
      onClose();
      setIsDateTimePickerOpen(false); // 다른 행 열면 달력은 무조건 닫히게
    },
    {
      ignoreSelector: '[data-slot="popover-content"], [data-slot="table-row"]', // CalendarMonthPopover 외부를 클릭했을 때는 사이드 패널이 닫히지 않도록 예외 처리
      enabled: isOpen,
    },
  );

  return (
    <div
      ref={panelRef}
      className={clsx(
        'z-sidebar fixed top-0 right-0',
        'flex flex-col gap-8 pb-20',
        'scrollbar h-dvh w-100 overflow-auto',
        'border-line-normal-normal bg-background-normal border-l',
        'transform transition-transform duration-300 ease-out',
        isOpen ? 'shadow-panel translate-x-0' : 'translate-x-full',
        !isOpen && 'pointer-events-none', // 완전히 닫혀있을 때 화면 밖에서 클릭을 가로채지 못하게 방어
      )}
    >
      <div className="flex items-center justify-between p-4">
        <Icon
          color="text-label-neutral"
          iconName="ChevronForward"
          width={24}
          height={24}
          onClick={onClose}
        />
        <div className="flex items-center gap-2">
          <Button variant="solid" onClick={handleSubmit}>
            저장
          </Button>
          <Button>삭제</Button>
        </div>
      </div>
      <div className="flex flex-col gap-10 px-5">
        <textarea
          ref={titleRef}
          className={clsx(
            'heading1-bold text-label-strong placeholder:text-label-assistive',
            'resize-none overflow-hidden border-0 leading-tight outline-0',
          )}
          value={title}
          onChange={(e) => {
            setTitle(e.target.value.trimStart());
          }}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault();
            }
          }}
          placeholder="거래처를 입력해 주세요."
        />
        <div className="relative">
          <ValueContainer items={valueItems} />
          {isDateTimePickerOpen && (
            <div className="absolute top-9 right-0 z-10">
              <DateTimePicker
                initialDateTime={selectedDateTime}
                onDateTimeSelect={setSelectedDateTime}
                onClose={() => setIsDateTimePickerOpen(false)}
              />
            </div>
          )}
        </div>
        <Divider style="thin" />
        <MoneyContainer
          rateUpdatedAt={selectedDateTime ?? undefined}
          onValuesChange={setCurrencyValues}
        />
        <Divider style="thin" />
        <TextInput
          value={memo}
          onChange={setMemo}
          title="메모"
          placeholder="메모를 입력해 주세요."
        />
        {mode && mode !== 'manual' && (
          <>
            <Divider style="thin" />
            <div className="flex flex-col gap-2">
              <p className="label1-normal-bold text-label-neutral">
                {uploadTitleMap[mode]}
              </p>
              {/* @TODO: 파일 또는 이미지 미리보기 컴포넌트 추가 예정 */}
              {mode === 'file' && <p>파일 미리보기</p>}
              {mode === 'image' && <p>사진 미리보기</p>}
            </div>
          </>
        )}
      </div>
    </div>
  );
};
export default SidePanelUI;
