import { useCallback, useEffect, useMemo, useState } from 'react';

import { useDropZone } from '@/components/chart/widget/hook/useWidgetDragAndDrop';
import { MOCK_WIDGET_DATA } from '@/components/chart/widget/mock';
import {
  WIDGET_TYPES,
  type WidgetItem,
  type WidgetType,
} from '@/components/chart/widget/type';

const WIDGET_MAX_SLOT_BREAKPOINT = 1500;
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

// 위젯 CRUD + 표시 로직
const useWidgetCRUD = (maxWidgets: number) => {
  // TODO: 추가된 위젯 목록 (API 연동 전까지 mock 활용)
  const [addedWidgets, setAddedWidgets] =
    useState<WidgetItem[]>(MOCK_WIDGET_DATA);

  // 삭제된 위젯 타입 목록 (순서 유지: 가장 최근 삭제된 위젯이 맨 뒤)
  const [removedWidgets, setRemovedWidgets] = useState<WidgetType[]>([]);

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
    // 삭제 이력 없는 위젯 (WIDGET_TYPES 원래 순서)
    const initialAvailableWidgets = WIDGET_TYPES.filter(
      (type) => !addedTypes.has(type) && !removedWidgets.includes(type),
    );
    // 삭제 이력 있는 위젯 (삭제된 순서 유지)
    const removed = removedWidgets.filter((type) => !addedTypes.has(type));
    return [...initialAvailableWidgets, ...removed];
  }, [addedWidgets, removedWidgets]);

  // 특정 위치(targetOrder)에 위젯 삽입
  const handleInsertWidget = useCallback(
    (widgetType: WidgetType, targetOrder: number) => {
      setAddedWidgets((prev) => {
        // 용량 체크
        if (getTotalSlots(prev) + getWidgetSpan(widgetType) > maxWidgets)
          return prev;

        const normalized = normalizeOrders(prev);
        const shifted = normalized.map((w) =>
          w.order >= targetOrder ? { ...w, order: w.order + 1 } : w,
        );
        return normalizeOrders([
          ...shifted,
          { order: targetOrder, widgetType },
        ]);
      });

      setRemovedWidgets((prev) => prev.filter((t) => t !== widgetType));
    },
    [maxWidgets],
  );

  // 위젯을 맨 뒤에 추가
  const handleAddWidget = useCallback(
    (widgetType: WidgetType) => {
      setAddedWidgets((prev) => {
        if (getTotalSlots(prev) + getWidgetSpan(widgetType) > maxWidgets)
          return prev;

        const normalized = normalizeOrders(prev);
        return [...normalized, { order: normalized.length, widgetType }];
      });

      setRemovedWidgets((prev) => prev.filter((t) => t !== widgetType));
    },
    [maxWidgets],
  );

  // list 내에서 위젯 순서 이동
  const handleMoveWidget = useCallback((fromOrder: number, toOrder: number) => {
    // 같은 자리에 놓는 경우 (자기 왼쪽/오른쪽 gap) 무시
    if (toOrder === fromOrder || toOrder === fromOrder + 1) return;

    setAddedWidgets((prev) => {
      const normalized = normalizeOrders(prev);

      // 이동할 위젯 (타겟 위젯)
      const widget = normalized.find((w) => w.order === fromOrder);
      if (!widget) return prev;

      // 타겟 위젯을 제외한 나머지 위젯들
      const without = normalizeOrders(
        normalized.filter((w) => w.order !== fromOrder),
      );

      // 순서 보정 (앞에서 타겟 위젯이 빠졌으니 -1)
      const adjustedTo = toOrder > fromOrder ? toOrder - 1 : toOrder;

      // 타겟 위치 이후 위젯들 순서 +1
      const shifted = without.map((w) =>
        w.order >= adjustedTo ? { ...w, order: w.order + 1 } : w,
      );
      return normalizeOrders([...shifted, { ...widget, order: adjustedTo }]);
    });
  }, []);

  const handleRemoveWidget = useCallback((order: number) => {
    setAddedWidgets((prev) => {
      const target = prev.find((w) => w.order === order);
      if (target && target.widgetType !== 'BLANK') {
        const type = target.widgetType as WidgetType;
        // 삭제 이력에 추가 (이미 있으면 제거 후 맨 뒤에)
        setRemovedWidgets((old) => [...old.filter((t) => t !== type), type]);
      }
      return normalizeOrders(prev.filter((w) => w.order !== order));
    });
  }, []);

  return {
    displayWidgets,
    availableWidgets,
    handleAddWidget,
    handleInsertWidget,
    handleMoveWidget,
    handleRemoveWidget,
  };
};

const getMaxWidgets = () => {
  return window.innerWidth >= WIDGET_MAX_SLOT_BREAKPOINT
    ? MAX_WIDGET_SLOTS
    : MIN_WIDGET_SLOTS;
};

// 조합 레이어
export const useWidgetManager = () => {
  const [isWidgetEditMode, setIsWidgetEditMode] = useState(false);
  const [maxWidgets, setMaxWidgets] = useState(getMaxWidgets());

  useEffect(() => {
    const updateLayout = () => {
      const newMax = getMaxWidgets();
      setMaxWidgets((prev) => (prev !== newMax ? newMax : prev));
    };

    updateLayout();
    window.addEventListener('resize', updateLayout);
    return () => window.removeEventListener('resize', updateLayout);
  }, []);

  const toggleEditMode = useCallback(
    () => setIsWidgetEditMode((prev) => !prev),
    [],
  );

  const {
    displayWidgets,
    availableWidgets,
    handleAddWidget,
    handleInsertWidget,
    handleMoveWidget,
    handleRemoveWidget,
  } = useWidgetCRUD(maxWidgets);

  // list 영역 전체 드롭존 (gap에 안 떨어졌을 때 fallback → 맨 뒤에 추가)
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
    handleInsertWidget,
    handleMoveWidget,
    handleRemoveWidget,
    listDropZone,
    pickerDropZone,
  };
};
