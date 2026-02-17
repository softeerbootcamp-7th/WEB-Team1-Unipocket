import WidgetListItem from '@/components/chart/widget/components/WidgetListItem';
import type { WidgetItem } from '@/components/chart/widget/type';
import { useWidgetContext } from '@/components/chart/widget/WidgetContext';

interface WidgetListProps {
  displayWidgets: WidgetItem[];
  handleRemoveWidget: (order: number) => void;
  dropZoneProps?: React.HTMLAttributes<HTMLDivElement>;
}

const WidgetList = ({
  displayWidgets,
  handleRemoveWidget,
  dropZoneProps,
}: WidgetListProps) => {
  const { isWidgetEditMode } = useWidgetContext();
  return (
    <div
      className="rounded-modal-20 flex w-full items-center justify-center gap-5 transition-colors"
      {...dropZoneProps}
    >
      {displayWidgets.map((widget, i) => (
        <WidgetListItem
          key={widget.widgetType === 'BLANK' ? `BLANK-${i}` : widget.widgetType}
          widget={widget}
          isWidgetEditMode={isWidgetEditMode}
          handleRemoveWidget={handleRemoveWidget}
          isFirst={i === 0}
        />
      ))}
    </div>
  );
};

export default WidgetList;
