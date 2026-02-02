import { useState } from 'react';

import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import Icon from '@/components/common/Icon';
import TextInput from '@/components/common/TextInput';

import CurrencyConverter from './CurrencyConverter';

const SidePanel = () => {
  const [memo, setMemo] = useState('');

  const title = 'Coles (Wollongong Central) Groceries';

  return (
    <div className="flex w-100 flex-col gap-8 pb-50 border-l border-line-normal-normal bg-background-normal shadow-panel">
      <div className="flex p-4 justify-between items-center">
        <Icon color="text-label-neutral" iconName="ChevronForward" width={24} height={24} />
        <div className="flex gap-2">
            <Button>저장</Button>
            <Button>삭제</Button>
        </div>
      </div>
      <div className="flex flex-col gap-10 px-5">
        <div className="heading1-bold text-label-strong word-break">{title}</div>
        <div>value container</div>
        <Divider style="thin" />
        <CurrencyConverter />
        <Divider style="thin" />
        <TextInput
          value={memo}
          onChange={setMemo}
          title="메모"
          placeholder="메모를 입력해 주세요."
        />
        <Divider style="thin" />
        <div>uploader</div>
      </div>
    </div>
  );
};

export default SidePanel;
