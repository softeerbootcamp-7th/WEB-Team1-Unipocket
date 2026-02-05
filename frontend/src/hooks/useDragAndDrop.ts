import { useCallback, useEffect, useRef, useState } from 'react';

export function useDragAndDrop(onDropFiles: (files: FileList) => void) {
  const [isDragging, setIsDragging] = useState(false);
  const onDropFilesRef = useRef(onDropFiles);

  useEffect(() => {
    onDropFilesRef.current = onDropFiles;
  }, [onDropFiles]);

  // 드래그 영역에 들어왔을 때
  const onDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  }, []);

  // 드래그 영역에서 벗어났을 때
  const onDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  }, []);

  // 드롭했을 때
  const onDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    if (!e.dataTransfer) {
      return;
    }

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      onDropFilesRef.current(files);
    }
  }, []);

  return {
    isDragging,
    bind: {
      onDragOver,
      onDragLeave,
      onDrop,
    },
  };
}
