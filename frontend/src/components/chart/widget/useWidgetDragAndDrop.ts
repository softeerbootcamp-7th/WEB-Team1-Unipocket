import { useCallback, useRef, useState } from 'react';

import type { WidgetItem, WidgetType } from '@/components/chart/widget/type';

export interface DragData {
  widgetType: WidgetType;
  source: 'list' | 'picker';
  order?: number;
}

const DRAG_DATA_KEY = 'application/widget-drag';

export const setDragData = (e: React.DragEvent, data: DragData) => {
  e.dataTransfer.setData(DRAG_DATA_KEY, JSON.stringify(data));
  e.dataTransfer.effectAllowed = 'move';
};

export const getDragData = (e: React.DragEvent): DragData | null => {
  try {
    const raw = e.dataTransfer.getData(DRAG_DATA_KEY);
    return raw ? (JSON.parse(raw) as DragData) : null;
  } catch {
    return null;
  }
};

// Drag 요소
interface UseDraggableOptions {
  dragData?: DragData;
  isDraggable?: boolean;
}

export const useDraggable = ({
  dragData,
  isDraggable,
}: UseDraggableOptions) => {
  const [isDragging, setIsDragging] = useState(false);

  const onDragStart = useCallback(
    (e: React.DragEvent) => {
      if (!isDraggable || !dragData) {
        e.preventDefault();
        return;
      }

      setDragData(e, dragData);

      setIsDragging(true);
    },
    [dragData, isDraggable],
  );

  const onDragEnd = useCallback(() => {
    setIsDragging(false);
  }, []);

  return {
    isDragging,
    dragHandleProps: {
      draggable: isDraggable,
      onDragStart,
      onDragEnd,
    },
  };
};

// Drop 영역
interface UseDropZoneOptions {
  zone: 'list' | 'picker';
  onDropWidget: (data: DragData, dropOrder?: number) => void;
}

export const useDropZone = ({ zone, onDropWidget }: UseDropZoneOptions) => {
  const [isDragOver, setIsDragOver] = useState(false);
  const dragCounterRef = useRef(0);

  const onDragEnter = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    dragCounterRef.current += 1;
    setIsDragOver(true);
  }, []);

  const onDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  }, []);

  const onDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    dragCounterRef.current -= 1;
    if (dragCounterRef.current <= 0) {
      dragCounterRef.current = 0;
      setIsDragOver(false);
    }
  }, []);

  const onDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      dragCounterRef.current = 0;
      setIsDragOver(false);

      const data = getDragData(e);
      if (!data) return;

      // 같은 영역에서의 드롭은 무시
      if (data.source === zone) return;

      onDropWidget(data);
    },
    [zone, onDropWidget],
  );

  return {
    isDragOver,
    dropZoneProps: {
      onDragEnter,
      onDragOver,
      onDragLeave,
      onDrop,
    },
  };
};

interface UseWidgetDragAndDropOptions {
  handleAddWidget: (widgetType: WidgetType) => void;
  handleRemoveWidget: (order: number) => void;
  displayWidgets: WidgetItem[];
}

export const useWidgetDragAndDrop = ({
  handleAddWidget,
  handleRemoveWidget,
}: UseWidgetDragAndDropOptions) => {
  const onDropToList = useCallback(
    (data: DragData) => {
      if (data.source === 'picker') {
        handleAddWidget(data.widgetType);
      }
    },
    [handleAddWidget],
  );

  const onDropToPicker = useCallback(
    (data: DragData) => {
      if (data.source === 'list' && data.order !== undefined) {
        handleRemoveWidget(data.order);
      }
    },
    [handleRemoveWidget],
  );

  const listDropZone = useDropZone({
    zone: 'list',
    onDropWidget: onDropToList,
  });
  const pickerDropZone = useDropZone({
    zone: 'picker',
    onDropWidget: onDropToPicker,
  });

  return {
    listDropZone,
    pickerDropZone,
  };
};
