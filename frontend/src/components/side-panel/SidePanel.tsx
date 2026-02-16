import { useLayoutEffect, useRef, useState } from 'react';
import { clsx } from 'clsx';

import { useClickOutside } from '@/hooks/useClickOutside';

import { formatDateTime } from '@/components/calendar/date.utils';
import Button from '@/components/common/Button';
import Chip from '@/components/common/Chip';
import Divider from '@/components/common/Divider';
import Icon from '@/components/common/Icon';
import TextInput from '@/components/common/TextInput';
import { useDataTable } from '@/components/data-table/context';
import type { Expense } from '@/components/landing-page/dummy';
import DateTimePicker from '@/components/side-panel/DateTimePicker';
import MoneyContainer from '@/components/side-panel/MoneyContainer';
import ValueContainer, {
  type ValueItemProps,
} from '@/components/side-panel/ValueContainer';

type SidePanelInputMode = 'manual' | 'file' | 'image';

const uploadTitleMap: Record<Exclude<SidePanelInputMode, 'manual'>, string> = {
  file: '파일',
  image: '사진',
};

// @TODO: API 연동 시 file / image 모드에서 파일 데이터 props로 받기
interface SidePanelProps {
  mode?: SidePanelInputMode;
  file?: File; // file / image 전용
}

const SidePanel = ({ mode = 'manual' }: SidePanelProps) => {
  const { tableState, dispatch } = useDataTable();
  const { activeRow } = tableState;

  const isOpen = !!activeRow;
  const panelRef = useRef<HTMLDivElement>(null);

  const [title, setTitle] = useState('');
  const [memo, setMemo] = useState('');
  const [isDateTimePickerOpen, setIsDateTimePickerOpen] = useState(false);
  const [selectedDateTime, setSelectedDateTime] = useState<Date | null>(null);
  //const isEditing = false; // @TODO: 수정 시 사용 예정 (useDebouncedEffect 훅 연결)

  const [prevRowId, setPrevRowId] = useState<string | null>(null);

  if (activeRow && activeRow.rowId !== prevRowId) {
    const rowData = activeRow.value as Expense;
    setPrevRowId(activeRow.rowId);
    setTitle(rowData.merchantName || '');
    setMemo(rowData.memo || '');
    setSelectedDateTime(rowData.date ? new Date(rowData.date) : null);
    setIsDateTimePickerOpen(false); // 다른 행 열면 달력은 무조건 닫히게
  }

  // activeRow.value 데이터를 기반으로 화면에 그릴 내용을 정의합니다.
  const rowData = activeRow?.value as Expense | undefined;

  const valueItems: ValueItemProps[] = [
    {
      label: '일시',
      // 사용자가 달력에서 새 날짜를 선택했다면 그걸 보여주고, 아니면 기존 데이터를 보여줌
      value: selectedDateTime ? formatDateTime(selectedDateTime) : '비어 있음',
      onClick: () => {
        setIsDateTimePickerOpen((prev) => !prev);
      },
    },
    {
      label: '카테고리',
      value: rowData?.categoryCode ? (
        <Chip type={rowData.categoryCode} /> // Chip 컴포넌트가 해당 타입을 받게 되어 있다고 가정
      ) : (
        '-'
      ),
    },
    {
      label: '결제 수단',
      // 현금인지 카드인지에 따라 표시 형식 변경
      value: rowData?.paymentMethod
        ? rowData.paymentMethod.isCash
          ? '현금'
          : rowData.paymentMethod.card?.label || '-'
        : '-',
    },
    {
      label: '여행',
      value: rowData?.travel?.name || '-',
    },
  ];

  const titleRef = useRef<HTMLTextAreaElement>(null);

  useLayoutEffect(() => {
    if (!titleRef.current) return;
    titleRef.current.style.height = '0px';
    titleRef.current.style.height = `${titleRef.current.scrollHeight}px`;
  }, [title]);

  const handleDateTimeSelect = (selected: Date) => {
    setSelectedDateTime(selected);
    // setValueItems((prev) =>
    //   prev.map((item) =>
    //     item.label === '일시'
    //       ? { ...item, value: formatDateTime(selected) }
    //       : item,
    //   ),
    // );
  };

  const closePanel = () => {
    dispatch({ type: 'SET_ACTIVE_ROW', payload: null });
  };

  useClickOutside(panelRef, () => {
    closePanel();
  });

  return (
    <div
      ref={panelRef}
      className={clsx(
        'fixed top-0 right-0 z-(--z-sidebar)',
        'flex flex-col gap-8 pb-50',
        'scrollbar h-dvh w-100 overflow-auto',
        'border-line-normal-normal bg-background-normal shadow-panel border-l',
        // 애니메이션 속성
        'transform transition-transform duration-300 ease-out',
        // 열려있으면 원래 위치로(0), 닫혀있으면 화면 우측 밖으로 100% 밀어냄
        isOpen ? 'translate-x-0' : 'translate-x-full',
        // 완전히 닫혀있을 때 화면 밖에서 클릭을 가로채지 못하게 방어
        !isOpen && 'pointer-events-none',
      )}
    >
      <div className="flex items-center justify-between p-4">
        <Icon
          color="text-label-neutral"
          iconName="ChevronForward"
          width={24}
          height={24}
          onClick={closePanel}
        />
        <div className="flex items-center gap-2">
          {/*<div className="flex gap-1 items-center">
            {isEditing ? (
              <>
                <p className="label2-medium text-label-alternative">저장 중...</p>
                <Icons.Loading className="h-3 w-3 animate-spin text-label-assistive" />
              </>
            ) : (
              <>
                <p className="label2-medium text-label-alternative">저장됨</p>
                <Icons.CheckmarkCircle className="h-3 w-3" />
              </>
            )}
          </div>*/}
          <Button variant="solid">저장</Button>
          <Button>삭제</Button>
        </div>
      </div>
      <div className="flex flex-col gap-10 px-5">
        <textarea
          ref={titleRef}
          className="heading1-bold text-label-strong placeholder:text-label-assistive resize-none overflow-hidden border-0 leading-tight outline-0"
          value={title}
          placeholder="거래처를 입력해 주세요."
          onChange={(e) => {
            setTitle(e.target.value);
          }}
        />
        <div className="relative">
          <ValueContainer items={valueItems} />
          {isDateTimePickerOpen && (
            <div className="absolute top-9 right-0 z-10">
              <DateTimePicker
                initialDateTime={selectedDateTime}
                onDateTimeSelect={handleDateTimeSelect}
                onClose={() => setIsDateTimePickerOpen(false)}
              />
            </div>
          )}
        </div>
        <Divider style="thin" />
        <MoneyContainer />
        <Divider style="thin" />
        <TextInput
          value={memo}
          onChange={setMemo}
          title="메모"
          placeholder="메모를 입력해 주세요."
        />

        {mode !== 'manual' && (
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

export default SidePanel;
