import SidePanelUI from '@/components/side-panel/SidePanelUI';
import type { SidePanelFormValues } from '@/components/side-panel/type';

import { useCreateManualExpenseMutation } from '@/api/expenses/query';
import type { CreateManualExpenseRequest } from '@/api/expenses/type';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

interface ManualCreatePanelProps {
  isOpen: boolean;
  onClose: () => void;
}

const ManualCreatePanel = ({ isOpen, onClose }: ManualCreatePanelProps) => {
  const { mutate } = useCreateManualExpenseMutation();
  const accountBookId = useAccountBookStore((state) => state.accountBook?.id);

  const handleSubmit = (values: SidePanelFormValues) => {
    const request: CreateManualExpenseRequest = {
      ...values,
      occurredAt: values.occurredAt.toISOString(),
    };

    mutate(
      { accountBookId: Number(accountBookId), data: request },
      {
        onSuccess: () => {
          onClose();
        },
      },
    );
  };

  return (
    <SidePanelUI
      mode="manual"
      isOpen={isOpen}
      onClose={onClose}
      onSubmit={handleSubmit}
    />
  );
};

export default ManualCreatePanel;
