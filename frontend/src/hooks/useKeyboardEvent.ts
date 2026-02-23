import { useEffect } from 'react';

export const useEscapeKey = (isOpen: boolean, onClose: () => void) => {
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);
};

export const useEnterKey = (
  isOpen: boolean,
  onAction: () => void,
  enabled: boolean = true,
) => {
  useEffect(() => {
    if (!isOpen || !enabled) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Enter') onAction();
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onAction, enabled]);
};
