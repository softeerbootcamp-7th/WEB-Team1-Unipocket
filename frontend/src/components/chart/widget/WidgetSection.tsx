import { useWidgetManager } from '@/components/chart/widget/useWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import WidgetHeader from '@/components/chart/widget/WidgetHeader';
import WidgetList from '@/components/chart/widget/WidgetList';
import WidgetPicker from '@/components/chart/widget/WidgetPicker';

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
          />
        </div>

        {isWidgetEditMode && (
          <WidgetPicker
            maxWidgets={maxWidgets}
            availableWidgets={availableWidgets}
            handleAddWidget={handleAddWidget}
          />
        )}
      </div>
    </WidgetContext.Provider>
  );
};

export default WidgetSection;
