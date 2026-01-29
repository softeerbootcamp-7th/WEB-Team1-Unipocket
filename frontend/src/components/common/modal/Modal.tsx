import React, { useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';

import Button, { type ButtonProps } from '../Button';
import Icon from '../Icon';
import { ModalContext } from './useModalContext';

type ActionType = 'confirm' | 'next' | 'delete';

const BUTTON_CONFIG: Record<
  ActionType,
  { text: string; variant: NonNullable<ButtonProps['variant']> }
> = {
  confirm: { text: '확인', variant: 'solid' },
  next: { text: '다음', variant: 'solid' },
  delete: { text: '삭제', variant: 'danger' },
};

export interface ModalProps {
  children: React.ReactNode;
  isOpen: boolean;
  onClose: () => void;
  onAction: () => void;
  actionType?: ActionType;
}

/**
 * Modal 컴포넌트 (Container/Wrapper)
 *
 * - Context API를 통해 내부 컨텐츠(children)의 유효성 검사 상태(isActionReady)를 관리합니다.
 * - useModalContext는 선택적으로 사용: 버튼 블로킹이 필요한 경우에만 자식 컴포넌트에서 호출하세요.
 * - ActionType에 따라 버튼의 텍스트와 스타일이 자동으로 변경됩니다.
 * - ActionType: 'confirm': '확인' | 'next': '다음' | 'delete': '삭제' 로 매핑됩니다.
 */
const Modal = ({
  children,
  isOpen,
  onClose,
  onAction,
  actionType = 'confirm',
}: ModalProps) => {
  // 모달의 확인 버튼 활성화 여부 (기본값 true: 단순 알림 모달 등을 위해)
  const [isActionReady, setActionReady] = useState(true);

  const { text: confirmText, variant: confirmVariant } =
    BUTTON_CONFIG[actionType];

  const handleBackdropClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <ModalContext.Provider value={{ setActionReady }}>
          {/* Backdrop */}
          <motion.div
            className="bg-dimmer-strong fixed inset-0 z-50 flex items-center justify-center"
            onClick={handleBackdropClick}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
          >
            {/* Modal Content */}
            <motion.div
              className="rounded-modal-16 bg-background-normal relative z-51 flex h-fit w-fit flex-col items-center justify-center px-4 pt-5"
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
                <Button variant="outlined" size="lg" onClick={onClose}>
                  취소
                </Button>
                <Button
                  variant={confirmVariant}
                  size="lg"
                  onClick={onAction}
                  disabled={!isActionReady}
                >
                  {confirmText}
                </Button>
              </div>
            </motion.div>
          </motion.div>
        </ModalContext.Provider>
      )}
    </AnimatePresence>
  );
};

export default Modal;
