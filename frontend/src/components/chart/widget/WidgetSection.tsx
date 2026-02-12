import { renderWidget } from '@/components/chart/widget/renderWidget';
import { useWidgetManager } from '@/components/chart/widget/useWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import ExpenseCard from '@/components/home-page/ExpenseCard';

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

  const ButtonVariant = isWidgetEditMode ? 'solid' : 'outlined';

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
          <div className="flex items-end gap-4">
            <ExpenseCard
              label="총 지출"
              baseCountryCode="KR"
              baseCountryAmount={1402432}
              localCountryCode="US"
              localCountryAmount={12232}
            />
            <Divider style="vertical" className="h-15" />
            <ExpenseCard
              label="이번 달 지출"
              baseCountryCode="KR"
              baseCountryAmount={200342}
              localCountryCode="US"
              localCountryAmount={12232}
            />
            <div className="flex-1" />
            <Button variant={ButtonVariant} size="md" onClick={toggleEditMode}>
              {isWidgetEditMode ? '위젯 편집 완료하기' : '위젯 편집하기'}
            </Button>
          </div>
          <div className="flex items-center justify-around">
            {displayWidgets
              .sort((a, b) => a.order - b.order)
              .map((widget) => (
                <div
                  key={widget.order}
                  onClick={
                    isWidgetEditMode && widget.widgetType !== 'BLANK'
                      ? () => handleRemoveWidget(widget.order)
                      : undefined
                  }
                  className={
                    isWidgetEditMode && widget.widgetType !== 'BLANK'
                      ? 'cursor-pointer'
                      : ''
                  }
                >
                  {renderWidget(widget)}
                </div>
              ))}
          </div>
        </div>

        {/* 위젯 편집 창 */}
        {isWidgetEditMode && (
          <div className="rounded-modal-20 shadow-semantic-subtle bg-background-normal z-50 mb-8 flex h-full flex-col items-start justify-between rounded-b-none px-10 py-9 pb-30">
            <div className="flex flex-col gap-2.75">
              <h3 className="title3-semibold text-label-normal">
                추가 가능한 위젯들
              </h3>
              <span className="label1-normal-medium text-label-neutral">
                추가하고 싶은 위젯을 상단에 드래그 해주세요.
                <br />
                {`위젯은 최대 ${maxWidgets}개까지 설정할 수 있습니다.`}
              </span>
            </div>
            {/* skeletion section */}
            <div className="w-full min-w-0 overflow-x-auto">
              <div className="flex w-max gap-8 py-1">
                {/* 스크롤 될 아이템들 (추가되지 않은 위젯들) */}
                {availableWidgets.map((widgetType) => (
                  <div
                    key={widgetType}
                    onClick={() => handleAddWidget(widgetType)}
                    className="cursor-pointer"
                  >
                    {renderWidget(
                      { order: -1, widgetType },
                      { isPreview: true },
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </WidgetContext.Provider>
  );
};

export default WidgetSection;
