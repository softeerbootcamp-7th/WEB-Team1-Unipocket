import { useLayoutEffect, useRef, useState } from 'react';

import Button from '@/components/common/Button';
import Category from '@/components/common/Category';
import Divider from '@/components/common/Divider';
import Icon from '@/components/common/Icon';
import TextInput from '@/components/common/TextInput';

import CurrencyConverter from './CurrencyConverter';
import DateTimePicker from './DateTimePicker';
import ValueContainer, { type ValueItemProps } from './ValueContainer';

export type SidePanelInputMode = 'manual' | 'file' | 'image';

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
  const [title, setTitle] = useState('');
  const [memo, setMemo] = useState('');
  const [isDateTimePickerOpen, setIsDateTimePickerOpen] = useState(false);
  const [selectedDateTime, setSelectedDateTime] = useState<Date | null>(null);
  //const isEditing = false; // @TODO: 수정 시 사용 예정 (useDebouncedEffect 훅 연결)

  const [valueItems, setValueItems] = useState<ValueItemProps[]>([
    {
      label: '일시',
      value: '비어 있음',
      onClick: () => {
        setIsDateTimePickerOpen((prev) => !prev);
      },
    },
    {
      label: '카테고리',
      value: <Category type="생활" />,
    },
    {
      label: '결제 수단',
      value: '하나 비바 X',
    },
    {
      label: '여행',
      value: '뉴욕',
    },
  ]);

  const ref = useRef<HTMLTextAreaElement>(null);

  useLayoutEffect(() => {
    if (!ref.current) return;
    ref.current.style.height = '0px';
    ref.current.style.height = `${ref.current.scrollHeight}px`;
  }, [title]);

  const formatDateTime = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const dayOfWeek = ['일', '월', '화', '수', '목', '금', '토'][date.getDay()];
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}.${month}.${day}. (${dayOfWeek}) ${hours}:${minutes}`;
  };

  const handleDateTimeSelect = (selected: Date) => {
    setSelectedDateTime(selected);
    setValueItems((prev) =>
      prev.map((item) =>
        item.label === '일시'
          ? { ...item, value: formatDateTime(selected) }
          : item,
      ),
    );
  };

  return (
    <div className="scrollbar border-line-normal-normal bg-background-normal shadow-panel fixed top-0 right-0 z-50 flex h-screen w-100 flex-col gap-8 overflow-y-auto border-l pb-50">
      <div className="flex items-center justify-between p-4">
        <Icon
          color="text-label-neutral"
          iconName="ChevronForward"
          width={24}
          height={24}
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
          ref={ref}
          className="heading1-bold text-label-strong placeholder:text-label-assistive resize-none overflow-hidden border-0 bg-transparent leading-tight outline-0"
          value={title}
          placeholder="거래처를 입력해 주세요."
          onChange={(e) => {
            setTitle(e.target.value);
          }}
        />
        <div className="relative">
          <ValueContainer items={valueItems} />
          {isDateTimePickerOpen && (
            <div className="absolute top-8 right-0 z-50 mt-2">
              <DateTimePicker 
                initialDateTime={selectedDateTime}
                onDateTimeSelect={handleDateTimeSelect}
                onClose={() => setIsDateTimePickerOpen(false)}
              />
            </div>
          )}
        </div>
        <Divider style="thin" />
        <CurrencyConverter />
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
