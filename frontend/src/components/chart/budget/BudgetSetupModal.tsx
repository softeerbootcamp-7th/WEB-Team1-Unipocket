import { useState } from 'react';

import CurrencyConverter from '@/components/currency/CurrencyConverter';
import Modal from '@/components/modal/Modal';

import { useContextualBudgetMutation } from '@/api/widget/query';

interface BudgetSetupModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialBudget?: number;
}

const BudgetSetupModal = ({
  isOpen,
  onClose,
  initialBudget,
}: BudgetSetupModalProps) => {
  const [budgetAmount, setBudgetAmount] = useState<number>(initialBudget ?? 0);

  const { mutate: updateBudget } = useContextualBudgetMutation();

  const handleAction = () => {
    updateBudget(budgetAmount, { onSuccess: onClose });
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} onAction={handleAction}>
      <div className="flex w-86 flex-col items-center">
        {/* title section */}
        <div className="flex flex-col items-center gap-1.5">
          <h2 className="headline1-bold text-label-normal text-center">
            예산을 설정해주세요
          </h2>
          <span className="text-label-alternative body1-normal-medium text-center">
            한가지 통화만 입력하면
            <br />
            나머지는 자동으로 설정됩니다
          </span>
        </div>

        {/* input section */}
        <div className="w-full py-13">
          <CurrencyConverter
            onBaseCurrencyChange={setBudgetAmount}
            initialBaseCurrency={initialBudget}
          />
        </div>
      </div>
    </Modal>
  );
};

export default BudgetSetupModal;
