import { useLayoutEffect, useRef, useState } from 'react';
import { clsx } from 'clsx';

import Button from '@/components/common/Button';
import { formatDateTime } from '@/components/common/calendar/date.utils';
import Chip from '@/components/common/Chip';
import Divider from '@/components/common/Divider';
import Icon from '@/components/common/Icon';
import CurrencyConverter from '@/components/common/side-panel/CurrencyConverter';
import DateTimePicker from '@/components/common/side-panel/DateTimePicker';
import ValueContainer, {
  type ValueItemProps,
} from '@/components/common/side-panel/ValueContainer';
import TextInput from '@/components/common/TextInput';

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
  const [isOpen, setIsOpen] = useState(true);
  const [isMounted, setIsMounted] = useState(true);
  const [title, setTitle] = useState('');
  const [memo, setMemo] = useState('');
  const [isDateTimePickerOpen, setIsDateTimePickerOpen] = useState(false);
  const [selectedDateTime, setSelectedDateTime] = useState<Date | null>(null);
  //const isEditing = false; // @TODO: 수정 시 사용 예정 (useDebouncedEffect 훅 연결)

  // @TODO: '일시' 제외 항목들에는 context menu 추가 예정
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
      value: <Chip type="생활" />,
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

  const titleRef = useRef<HTMLTextAreaElement>(null);

  useLayoutEffect(() => {
    if (!titleRef.current) return;
    titleRef.current.style.height = '0px';
    titleRef.current.style.height = `${titleRef.current.scrollHeight}px`;
  }, [title]);

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

  const closePanel = () => {
    setIsOpen(false);
    setTimeout(() => {
      setIsMounted(false);
    }, 300);
  };

  if (!isMounted) return null;
  return (
    <div
      className={clsx(
        'scrollbar border-line-normal-normal bg-background-normal shadow-panel fixed top-0 right-0 z-50 flex h-screen w-100 flex-col gap-8 overflow-y-auto border-l pb-50',
        'transform transition-transform duration-300 ease-out',
        isOpen ? 'translate-x-0' : 'translate-x-full',
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
