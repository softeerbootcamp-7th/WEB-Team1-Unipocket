import CurrencyConverter from '@/components/currency/CurrencyConverter';
import Modal from '@/components/modal/Modal';

interface BudgetSetupModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const BudgetSetupModal = ({ isOpen, onClose }: BudgetSetupModalProps) => {
  return (
    // TODO: API 연동 시 onAction 연동 필요
    <Modal isOpen={isOpen} onClose={onClose} onAction={() => {}}>
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
          <CurrencyConverter />
        </div>
      </div>
    </Modal>
  );
};

export default BudgetSetupModal;
