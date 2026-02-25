import React, { type ReactNode, useMemo, useState } from 'react';
import { createPortal } from 'react-dom';
import { clsx } from 'clsx';
import { AnimatePresence, motion } from 'framer-motion';

import { useEnterKey, useEscapeKey } from '@/hooks/useKeyboardEvent';

import Button, { type ButtonProps } from '@/components/common/Button';
import Icon from '@/components/common/Icon';
import { ModalContext } from '@/components/modal/useModalContext';

export interface ModalButton {
  label?: ReactNode;
  variant?: NonNullable<ButtonProps['variant']>;
}

export interface ModalProps {
  children: React.ReactNode;
  isOpen: boolean;
  onClose: () => void;
  onAction: () => void;
  className?: string;
  cancelButton?: ModalButton | null; // null로 전달 시 버튼 숨김
  confirmButton?: ModalButton | null; // null로 전달 시 버튼 숨김
}

/**
 * Modal 컴포넌트 (Container/Wrapper)
 *
 * - Context API를 통해 내부 컨텐츠(children)의 유효성 검사 상태(isActionReady)를 관리
 * - useModalContext는 선택적으로 사용: 버튼 블로킹이 필요한 경우에만 자식 컴포넌트에서 호출
 */
const Modal = ({
  children,
  isOpen,
  onClose,
  onAction,
  className,
  cancelButton,
  confirmButton,
}: ModalProps) => {
  // 모달의 확인 버튼 활성화 여부 (기본값 true: 단순 알림 모달 등을 위해)
  const [isActionReady, setActionReady] = useState(true);
  const contextValue = useMemo(
    () => ({ setActionReady, onAction }),
    [onAction],
  );

  // null 여부로 버튼 표시 여부 결정 (undefined는 기본값 적용, null은 버튼 숨김)
  const showCancel = cancelButton !== null;
  const showConfirm = confirmButton !== null;

  useEscapeKey(isOpen, onClose);
  useEnterKey(isOpen, onAction, isActionReady);

  const { label: cancelLabel = '취소', variant: cancelVariant = 'outlined' } =
    cancelButton ?? {};
  const { label: confirmLabel = '확인', variant: confirmVariant = 'solid' } =
    confirmButton ?? {};

  const handleBackdropClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return createPortal(
    <AnimatePresence>
      {isOpen && (
        <ModalContext.Provider value={contextValue}>
          {/* Backdrop */}
          <motion.div
            className="bg-dimmer-strong z-overlay fixed inset-0 flex items-center justify-center"
            onClick={handleBackdropClick}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
          >
            {/* Modal Content */}
            <motion.div
              className={clsx(
                'rounded-modal-16 bg-background-normal relative z-51 flex h-fit w-fit flex-col items-center justify-center px-4 pt-5',
                className,
              )}
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              transition={{ duration: 0.2 }}
            >
              <div className="flex w-full justify-end">
                <Icon iconName="Close" onClick={onClose} />
              </div>

              {children}

              <div className="flex h-20 w-full flex-row items-center justify-end gap-3">
                {showCancel && (
                  <Button variant={cancelVariant} size="lg" onClick={onClose}>
                    {cancelLabel}
                  </Button>
                )}
                {showConfirm && (
                  <Button
                    variant={confirmVariant}
                    size="lg"
                    onClick={onAction}
                    disabled={!isActionReady}
                  >
                    {confirmLabel}
                  </Button>
                )}
              </div>
            </motion.div>
          </motion.div>
        </ModalContext.Provider>
      )}
    </AnimatePresence>,
    document.body,
  );
};

export default Modal;
