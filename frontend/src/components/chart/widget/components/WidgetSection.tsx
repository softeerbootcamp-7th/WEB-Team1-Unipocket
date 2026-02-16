import WidgetHeader from '@/components/chart/widget/components/WidgetHeader';
import WidgetList from '@/components/chart/widget/components/WidgetList';
import WidgetPicker from '@/components/chart/widget/components/WidgetPicker';
import { useWidgetManager } from '@/components/chart/widget/hook/useWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import AnimatedPanel from '@/components/layout/AnimatedPanel';

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
    <WidgetContext.Provider value={{ isWidgetEditMode, toggleEditMode }}>
      <div
        className="flex h-full w-full min-w-0 flex-col gap-4"
        style={{
          height: isWidgetEditMode ? '100dvh' : '100%',
        }}
      >
        <div className="flex flex-col gap-8">
          <WidgetHeader />
          <WidgetList
            displayWidgets={displayWidgets}
            handleRemoveWidget={handleRemoveWidget}
            dropZoneProps={
              isWidgetEditMode ? listDropZone.dropZoneProps : undefined
            }
          />
        </div>

        {isWidgetEditMode && (
          <AnimatedPanel
            isOpen={isWidgetEditMode}
            className="shadow-semantic-subtle max-h-140"
          >
            <WidgetPicker
              maxWidgets={maxWidgets}
              availableWidgets={availableWidgets}
              dropZoneProps={pickerDropZone.dropZoneProps}
            />
          </AnimatedPanel>
        )}
      </div>
    </WidgetContext.Provider>
  );
};

export default WidgetSection;
