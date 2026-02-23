import { useState } from 'react';

import { useKeyboardNavigation } from '@/hooks/useKeyboardNavigation';

import Chip from '@/components/common/Chip';
import { useDataTable } from '@/components/data-table/context';
import { DataTableOptionList } from '@/components/data-table/DataTableOptionList';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import type { ActiveCellState } from '@/components/data-table/type';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { useGetTravelsQuery } from '@/api/travels/query';

const TravelCellEditor = () => {
  const { tableState } = useDataTable();
  const { travelCell } = tableState;

  if (!travelCell) return null;

  return (
    <TravelCellEditorContent
      key={`${travelCell.rowId}-${travelCell.columnId}`}
      travelCell={travelCell}
    />
  );
};

const TravelCellEditorContent = ({
  travelCell,
}: {
  travelCell: ActiveCellState;
}) => {
  const { dispatch } = useDataTable();

  // travelCell.value가 travelId라고 가정하고 number 타입으로 캐스팅합니다.
  const [selectedTravelId, setSelectedTravelId] = useState<number>(
    Number(travelCell.value) || 0,
  );

  // API 호출 (데이터가 없을 때를 대비해 기본값 빈 배열 할당)
  const { data: travels = [] } = useGetTravelsQuery();

  // 키보드 네비게이션과 리스트 아이템에 사용할 option 배열 (travelId 기준)
  const options = travels.map((travel) => travel.travelId);

  // 화면에 보여줄 이름(travelPlaceName)을 찾는 헬퍼 함수
  const getTravelName = (travelId: number) => {
    const travel = travels.find((t) => t.travelId === travelId);
    return travel?.travelPlaceName || '알 수 없는 여행';
  };

  const initialIndex = Math.max(0, options.indexOf(selectedTravelId));

  const { activeIndex, setActiveIndex, handleKeyDown } =
    useKeyboardNavigation<number>({
      items: options,
      initialActiveIndex: initialIndex,
      onSelect: (selectedId) => {
        setSelectedTravelId(selectedId);
        handleSave();
      },
    });

  const handleSave = () => {
    dispatch({ type: 'SET_TRAVEL_CELL', payload: null });
  };

  const handleSelectAndSave = (selectedId: number) => {
    setSelectedTravelId(selectedId);
    dispatch({ type: 'SET_TRAVEL_CELL', payload: null });
  };

  return (
    <Popover open={true} onOpenChange={(open) => !open && handleSave()}>
      <PopoverTrigger asChild>
        <CellEditorAnchor rect={travelCell.rect} style={{ opacity: 0 }} />
      </PopoverTrigger>

      <PopoverContent
        align="start"
        sideOffset={0}
        className="rounded-modal-8 border-line-solid-normal shadow-semantic-subtle bg-background-normal flex w-75 flex-col p-0 outline-none"
        onInteractOutside={() => handleSave()}
        onKeyDown={handleKeyDown}
      >
        <DataTableOptionList
          items={options}
          activeIndex={activeIndex}
          setActiveIndex={setActiveIndex}
          isSelected={(item) => selectedTravelId === item}
          onSelect={handleSelectAndSave}
          // ID 대신 화면에는 여행 이름을 Chip으로 렌더링합니다.
          renderItem={(item) => <Chip label={getTravelName(item)} />}
        />
      </PopoverContent>
    </Popover>
  );
};

export default TravelCellEditor;
