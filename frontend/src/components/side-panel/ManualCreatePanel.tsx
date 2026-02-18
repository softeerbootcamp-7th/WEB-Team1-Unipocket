import SidePanelUI from '@/components/side-panel/SidePanelUI';

interface ManualCreatePanelProps {
  isOpen: boolean;
  onClose: () => void;
}

const ManualCreatePanel = ({ isOpen, onClose }: ManualCreatePanelProps) => {
  return (
    <SidePanelUI
      mode="manual"
      isOpen={isOpen}
      onClose={onClose}
      initialData={undefined}
    />
  );
};

export default ManualCreatePanel;
