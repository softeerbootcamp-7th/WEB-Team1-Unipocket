import { useState } from 'react';

import Divider from '../Divider';
import TextInput from '../TextInput';
import CurrencyConverter from './CurrencyConverter';

const SidePanel = () => {
  const [memo, setMemo] = useState('');

  const title = 'Coles (Wollongong Central) Groceries';

  return (
    <div className="flex w-100 flex-col gap-8 pb-50">
      <div className="p-4">navigation</div>
      <div className="flex flex-col gap-9 px-5">
        <div className="heading1-bold text-label-strong">{title}</div>
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
