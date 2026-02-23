import { ActionButton } from '@/components/data-table/bars/update/ActionButton';
import { ActionPopover } from '@/components/data-table/bars/update/ActionPopover';
import { TravelSelectorContent } from '@/components/data-table/selectors/TravelSelectorContent';

interface BulkTravelActionProps {
  onUpdate: (travelId: number) => void;
}

export const BulkTravelAction = ({ onUpdate }: BulkTravelActionProps) => {
  return (
    <ActionPopover
      renderContent={(close) => (
        <TravelSelectorContent
          align="center"
          sideOffset={16}
          onTravelSelect={(val) => {
            onUpdate(val);
            close();
          }}
        />
      )}
    >
      <ActionButton>여행</ActionButton>
    </ActionPopover>
  );
};
