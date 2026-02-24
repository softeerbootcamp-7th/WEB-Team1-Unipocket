import { ActionButton } from '@/components/data-table/bars/update/ActionButton';
import { ActionPopover } from '@/components/data-table/bars/update/ActionPopover';
import { useDataTable } from '@/components/data-table/context';
import { TravelSelectorContent } from '@/components/data-table/selectors/TravelSelectorContent';

import type { Expense } from '@/api/expenses/type';
import { NONE_TRAVEL } from '@/constants/column';

interface BulkTravelActionProps {
  onUpdate: (travelId: number | null) => void;
}

export const BulkTravelAction = ({ onUpdate }: BulkTravelActionProps) => {
  const { table } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;

  const getInitialTravelId = (): number | string | null => {
    if (selectedRows.length === 0) return null;

    const uniqueTravels = new Set(
      selectedRows.map((row) => {
        const original = row.original as Expense;
        return original.travel?.travelId ?? NONE_TRAVEL;
      }),
    );

    return uniqueTravels.size === 1 ? Array.from(uniqueTravels)[0] : null;
  };

  return (
    <ActionPopover
      renderContent={(close) => (
        <TravelSelectorContent
          align="center"
          sideOffset={16}
          initialTravelId={getInitialTravelId()}
          onTravelSelect={(val) => {
            const targetTravelId = val === NONE_TRAVEL ? null : Number(val);
            onUpdate(targetTravelId);
            close();
          }}
        />
      )}
    >
      <ActionButton>여행</ActionButton>
    </ActionPopover>
  );
};
