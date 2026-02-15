import { useWidgetDragAndDrop } from '@/components/chart/widget/useWidgetDragAndDrop';
import { useWidgetManager } from '@/components/chart/widget/useWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import WidgetHeader from '@/components/chart/widget/WidgetHeader';
import WidgetList from '@/components/chart/widget/WidgetList';
import WidgetPicker from '@/components/chart/widget/WidgetPicker';
import BottomSheet from '@/components/layout/BottomSheet';

const WidgetSection = () => {
  const {
    isWidgetEditMode,
    toggleEditMode,
    width,
    maxWidgets,
    displayWidgets,
    availableWidgets,
    handleAddWidget,
    handleRemoveWidget,
  } = useWidgetManager();

  const { listDropZone, pickerDropZone } = useWidgetDragAndDrop({
    handleAddWidget,
    handleRemoveWidget,
    displayWidgets,
  });

  return (
    <WidgetContext.Provider value={{ isEditMode: isWidgetEditMode }}>
      <div
        className="flex flex-col gap-4"
        style={{
          width: `${width}px`,
          height: isWidgetEditMode ? '100dvh' : '100%',
        }}
      >
        <div className="flex flex-col gap-8">
          <WidgetHeader
            isWidgetEditMode={isWidgetEditMode}
            toggleEditMode={toggleEditMode}
          />
          <WidgetList
            displayWidgets={displayWidgets}
            isWidgetEditMode={isWidgetEditMode}
            handleRemoveWidget={handleRemoveWidget}
            dropZoneProps={
              isWidgetEditMode ? listDropZone.dropZoneProps : undefined
            }
          />
        </div>

        {isWidgetEditMode && (
          <BottomSheet
            isOpen={isWidgetEditMode}
            className="shadow-semantic-subtle max-h-[47vh]"
            backdrop={false}
          >
            <WidgetPicker
              maxWidgets={maxWidgets}
              availableWidgets={availableWidgets}
              dropZoneProps={pickerDropZone.dropZoneProps}
            />
          </BottomSheet>
        )}
      </div>
    </WidgetContext.Provider>
  );
};

export default WidgetSection;
