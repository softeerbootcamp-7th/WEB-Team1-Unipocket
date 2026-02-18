import { useWidgetContext } from '@/components/chart/widget/WidgetContext';

import { Icons } from '@/assets';

const BlankChart = () => {
  const { isWidgetEditMode, toggleEditMode } = useWidgetContext();
  return (
    <div className="rounded-modal-16 border-line-solid-normal flex h-72 w-67 items-center justify-center border">
      {!isWidgetEditMode && (
        <button type="button" onClick={toggleEditMode}>
          <Icons.Add className="text-line-solid-strong size-6" />
        </button>
      )}
    </div>
  );
};

export default BlankChart;
