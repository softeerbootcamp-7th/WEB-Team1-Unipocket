import { useLayoutEffect, useRef, useState } from 'react';

import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import Icon from '@/components/common/Icon';
import TextInput from '@/components/common/TextInput';

import CurrencyConverter from './CurrencyConverter';
import ValueContainer from './ValueContainer';

const SidePanel = () => {
  const [title, setTitle] = useState('');
  const [memo, setMemo] = useState('');

  const ref = useRef<HTMLTextAreaElement>(null);

  useLayoutEffect(() => {
    if (!ref.current) return;
    ref.current.style.height = '0px';
    ref.current.style.height = `${ref.current.scrollHeight}px`;
  }, [title]);

  const showUploader = true;

  return (
    <div className="scrollbar border-line-normal-normal bg-background-normal shadow-panel fixed top-0 right-0 z-50 flex h-screen w-100 flex-col gap-8 overflow-y-auto border-l pb-50">
      <div className="flex items-center justify-between p-4">
        <Icon
          color="text-label-neutral"
          iconName="ChevronForward"
          width={24}
          height={24}
        />
        <div className="flex gap-2">
          <Button>저장</Button>
          <Button>삭제</Button>
        </div>
      </div>
      <div className="flex flex-col gap-10 px-5">
        <textarea
          ref={ref}
          className="heading1-bold text-label-strong resize-none overflow-hidden border-0 bg-transparent leading-tight outline-0"
          value={title}
          placeholder="제목을 입력해 주세요."
          onChange={(e) => {
            setTitle(e.target.value);
          }}
        />
        <ValueContainer />
        <Divider style="thin" />
        <CurrencyConverter />
        <Divider style="thin" />
        <TextInput
          value={memo}
          onChange={setMemo}
          title="메모"
          placeholder="메모를 입력해 주세요."
        />

        {showUploader && (
          <>
            <Divider style="thin" />
            <div>업로더</div>
          </>
        )}
      </div>
    </div>
  );
};

export default SidePanel;
