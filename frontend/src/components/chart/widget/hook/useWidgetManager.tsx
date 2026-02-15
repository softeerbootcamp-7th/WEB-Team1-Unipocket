import { useCallback, useEffect, useMemo, useState } from 'react';

import { useDropZone } from '@/components/chart/widget/hook/useWidgetDragAndDrop';
import { MOCK_WIDGET_DATA } from '@/components/chart/widget/mock';
import {
  WIDGET_TYPES,
  type WidgetItem,
  type WidgetType,
} from '@/components/chart/widget/type';

const DESKTOP_BREAKPOINT = 1500;
const MIN_WIDGET_SLOTS = 4;
const MAX_WIDGET_SLOTS = 5;

const getWidgetSpan = (widgetType: WidgetType | 'BLANK') =>
  widgetType === 'CATEGORY' ? 2 : 1;

const getTotalSlots = (widgets: WidgetItem[]) =>
  widgets.reduce((sum, w) => sum + getWidgetSpan(w.widgetType), 0);

const normalizeOrders = (widgets: WidgetItem[]): WidgetItem[] =>
  [...widgets]
    .sort((a, b) => a.order - b.order)
    .map((w, i) => ({ ...w, order: i }));

export const useWidgetManager = () => {
  const [isWidgetEditMode, setIsWidgetEditMode] = useState(false);
  const [maxWidgets, setMaxWidgets] = useState(MIN_WIDGET_SLOTS);

  useEffect(() => {
    const updateLayout = () => {
      const isDesktop = window.innerWidth >= DESKTOP_BREAKPOINT;
      setMaxWidgets(isDesktop ? MAX_WIDGET_SLOTS : MIN_WIDGET_SLOTS);
    };

    updateLayout();
    window.addEventListener('resize', updateLayout);
    return () => window.removeEventListener('resize', updateLayout);
  }, []);

  // TODO: 추가된 위젯 목록 (API 연동 전까지 mock 활용)
  const [addedWidgets, setAddedWidgets] =
    useState<WidgetItem[]>(MOCK_WIDGET_DATA);

  // 표시용 위젯 (실제 위젯 + 남은 슬롯은 BLANK로 채움)
  const displayWidgets: WidgetItem[] = useMemo(() => {
    let used = 0;
    const visible: WidgetItem[] = [];
    const sorted = normalizeOrders(addedWidgets);

    for (const widget of sorted) {
      const span = getWidgetSpan(widget.widgetType);
      if (used + span > maxWidgets) break;
      used += span;
      visible.push(widget);
    }

    const blanks: WidgetItem[] = Array.from(
      { length: maxWidgets - used },
      (_, i) => ({
        order: visible.length + i,
        widgetType: 'BLANK',
      }),
    );

    return [...visible, ...blanks];
  }, [addedWidgets, maxWidgets]);

  // 추가 가능한 위젯 타입 목록
  const availableWidgets = useMemo(() => {
    const addedTypes = new Set(addedWidgets.map((w) => w.widgetType));
    return WIDGET_TYPES.filter((type) => !addedTypes.has(type));
  }, [addedWidgets]);

  // 위젯을 맨 뒤에 추가
  const handleAddWidget = useCallback(
    (widgetType: WidgetType) => {
      setAddedWidgets((prev) => {
        if (getTotalSlots(prev) + getWidgetSpan(widgetType) > maxWidgets)
          return prev;

        const normalized = normalizeOrders(prev);
        return [...normalized, { order: normalized.length, widgetType }];
      });
    },
    [maxWidgets],
  );

  const handleRemoveWidget = useCallback((order: number) => {
    setAddedWidgets((prev) =>
      normalizeOrders(prev.filter((w) => w.order !== order)),
    );
  }, []);

  const toggleEditMode = useCallback(() => {
    setIsWidgetEditMode((prev) => !prev);
  }, []);

  const listDropZone = useDropZone({
    zone: 'list',
    onDropWidget: (data) => handleAddWidget(data.widgetType),
  });

  const pickerDropZone = useDropZone({
    zone: 'picker',
    onDropWidget: (data) => {
      if (data.order !== undefined) handleRemoveWidget(data.order);
    },
  });

  return {
    isWidgetEditMode,
    toggleEditMode,
    maxWidgets,
    displayWidgets,
    availableWidgets,
    handleAddWidget,
    handleRemoveWidget,
    listDropZone,
    pickerDropZone,
  };
};
