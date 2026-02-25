import { useLayoutEffect, useRef, useState } from 'react';
import { clsx } from 'clsx';
import { toast } from 'sonner';

import { useClickOutside } from '@/hooks/useClickOutside';

import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import Icon from '@/components/common/Icon';
import TextInput from '@/components/common/TextInput';
import type { CurrencyValues } from '@/components/currency/CurrencyConverter';
import { resolveUserCardId } from '@/components/data-table/utils/card';
import DateTimePicker from '@/components/side-panel/DateTimePicker';
import MoneyContainer from '@/components/side-panel/MoneyContainer';
import useSidePanelForm from '@/components/side-panel/useSidePanelForm';
import { useSidePanelValues } from '@/components/side-panel/useSidePanelValues';
import ValueContainer from '@/components/side-panel/ValueContainer';

import { useCreateManualExpenseMutation } from '@/api/expenses/query';
import type { CreateManualExpenseRequest } from '@/api/expenses/type';
import { useGetCardsQuery } from '@/api/users/query';
import { NONE_TRAVEL } from '@/constants/column';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

interface SidePanelProps {
  isOpen: boolean;
  onClose: () => void;
}

const SidePanel = ({ isOpen, onClose }: SidePanelProps) => {
  const panelRef = useRef<HTMLDivElement>(null);
  const titleRef = useRef<HTMLTextAreaElement>(null);

  const { mutate } = useCreateManualExpenseMutation();
  const accountBookId = useRequiredAccountBook().accountBookId;
  const { data: cards = [] } = useGetCardsQuery();

  const {
    title,
    setTitle,
    memo,
    setMemo,
    selectedDateTime,
    setSelectedDateTime,
    isDateTimePickerOpen,
    setIsDateTimePickerOpen,
    selectedCategory,
    setSelectedCategory,
    selectedCardNumber,
    setSelectedCardNumber,
    selectedTravelId,
    setSelectedTravelId,
    resetForm,
  } = useSidePanelForm();

  const [currencyValues, setCurrencyValues] = useState<CurrencyValues | null>(
    null,
  );
  const [resetKey, setResetKey] = useState(0);

  const valueItems = useSidePanelValues({
    selectedDateTime,
    onDateTimeClick: () => setIsDateTimePickerOpen((prev) => !prev),
    selectedCategory,
    onCategorySelect: setSelectedCategory,
    selectedCardNumber,
    onCardNumberSelect: setSelectedCardNumber,
    onCardNumberClear: () => setSelectedCardNumber(null),
    selectedTravelId,
    onTravelSelect: setSelectedTravelId,
    onTravelClear: () => setSelectedTravelId(null),
    allowDeselect: true,
  });

  const handleReset = () => {
    resetForm();
    setCurrencyValues(null);
    setResetKey((k) => k + 1);
  };

  const handleSubmit = () => {
    if (!title) {
      toast.error('거래처를 입력해 주세요.');
      return;
    }

    if (!currencyValues) {
      toast.error('금액을 입력해 주세요.');
      return;
    }

    const userCardId =
      selectedCardNumber != null
        ? (resolveUserCardId(selectedCardNumber, cards) ?? undefined)
        : undefined;

    const travelId =
      selectedTravelId != null && selectedTravelId !== NONE_TRAVEL
        ? Number(selectedTravelId)
        : undefined;

    const request: CreateManualExpenseRequest = {
      merchantName: title,
      category: selectedCategory,
      userCardId,
      occurredAt: selectedDateTime.toISOString(),
      localCurrencyAmount: currencyValues!.localAmount,
      localCurrencyCode: currencyValues!.localCurrencyCode,
      baseCurrencyAmount: currencyValues!.baseAmount,
      memo,
      travelId,
    };

    mutate(
      { accountBookId, data: request },
      {
        onSuccess: () => {
          onClose();
          handleReset();
        },
      },
    );
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
      handleReset();
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
          onClick={() => {
            onClose();
            handleReset();
          }}
        />
        <div className="flex items-center gap-2">
          <Button variant="solid" onClick={handleSubmit}>
            저장
          </Button>
          <Button onClick={handleReset}>초기화</Button>
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
          key={resetKey}
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
      </div>
    </div>
  );
};
export default SidePanel;
