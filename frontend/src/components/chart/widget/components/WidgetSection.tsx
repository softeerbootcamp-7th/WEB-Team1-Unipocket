import WidgetHeader from '@/components/chart/widget/components/WidgetHeader';
import WidgetList from '@/components/chart/widget/components/WidgetList';
import WidgetPicker from '@/components/chart/widget/components/WidgetPicker';
import { useWidgetManager } from '@/components/chart/widget/hook/useWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import BottomSheet from '@/components/layout/BottomSheet';

const WidgetSection = () => {
  const {
    isWidgetEditMode,
    toggleEditMode,
    maxWidgets,
    displayWidgets,
    availableWidgets,
    handleRemoveWidget,
    listDropZone,
    pickerDropZone,
  } = useWidgetManager();

  return (
    <WidgetContext.Provider value={{ isEditMode: isWidgetEditMode }}>
      <div
        className="flex w-full flex-col gap-4"
        style={{
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
            className="shadow-semantic-subtle max-h-[45vh]"
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
