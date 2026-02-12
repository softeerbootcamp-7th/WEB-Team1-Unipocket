import { useCallback, useEffect, useMemo, useState } from 'react';

import { MOCK_WIDGET_DATA } from '@/components/chart/widget/mock';
import { renderWidget } from '@/components/chart/widget/renderWidget';
import {
  WIDGET_TYPES,
  type WidgetItem,
  type WidgetType,
} from '@/components/chart/widget/type';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import ExpenseCard from '@/components/home-page/ExpenseCard';

const DESKTOP_BREAKPOINT = 1800;

const WidgetSection = () => {
  const [isWidgetEditMode, setIsWidgetEditMode] = useState(false);
  const [MAX_WIDGETS, setMaxWidgets] = useState(4);
  const [width, setWidth] = useState(1400);

  useEffect(() => {
    const updateMaxWidgets = () => {
      if (window.innerWidth >= DESKTOP_BREAKPOINT) {
        setMaxWidgets(5);
        setWidth(1600);
      } else {
        setMaxWidgets(4);
        setWidth(1400);
      }
    };

    updateMaxWidgets();
    window.addEventListener('resize', updateMaxWidgets);
    return () => window.removeEventListener('resize', updateMaxWidgets);
  }, []);

  // 추가된 위젯 종류. 순서대로. 일단 mock 활용
  const [addedWidgets, setAddedWidgets] =
    useState<WidgetItem[]>(MOCK_WIDGET_DATA);

  // 표시용 위젯 목록: 실제 위젯 + 남은 슬롯만큼 BLANK 채우기
  const displayWidgets: WidgetItem[] = useMemo(() => {
    let total = 0;
    const visible: WidgetItem[] = [];

    const sorted = [...addedWidgets].sort((a, b) => a.order - b.order);

    for (const widget of sorted) {
      const span = widget.widgetType === 'CATEGORY' ? 2 : 1;

      if (total + span > MAX_WIDGETS) break;

      total += span;
      visible.push(widget);
    }

    // 남은 슬롯 BLANK로 채우기
    const remaining = MAX_WIDGETS - total;

    const blanks: WidgetItem[] = Array.from({ length: remaining }, (_, i) => ({
      order: 1000 + i, // 기존 order랑 안 겹치게
      widgetType: 'BLANK',
    }));

    return [...visible, ...blanks];
  }, [addedWidgets, MAX_WIDGETS]);

  // 추가되지 않은 위젯 타입 필터링
  const availableWidgets = useMemo(() => {
    const addedTypes = new Set(addedWidgets.map((w) => w.widgetType));

    return WIDGET_TYPES.filter((type) => !addedTypes.has(type));
  }, [addedWidgets]);

  // 위젯 추가 (CATEGORY는 2슬롯 차지)
  const handleAddWidget = useCallback(
    (widgetType: WidgetType) => {
      setAddedWidgets((prev) => {
        const used = prev.reduce(
          (sum, w) => sum + (w.widgetType === 'CATEGORY' ? 2 : 1),
          0,
        );
        const needed = widgetType === 'CATEGORY' ? 2 : 1;
        if (used + needed > MAX_WIDGETS) return prev;
        const maxOrder = Math.max(...prev.map((w) => w.order), -1);
        return [...prev, { order: maxOrder + 1, widgetType }];
      });
    },
    [MAX_WIDGETS],
  );

  // 위젯 제거
  const handleRemoveWidget = useCallback((order: number) => {
    setAddedWidgets((prev) => prev.filter((w) => w.order !== order));
  }, []);

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
            <Button
              variant={ButtonVariant}
              size="md"
              onClick={() => setIsWidgetEditMode((prev) => !prev)}
            >
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
                {`위젯은 최대 ${MAX_WIDGETS}개까지 설정할 수 있습니다.`}
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
