import type { ComponentPropsWithoutRef } from 'react';
import clsx from 'clsx';
import { AnimatePresence, motion } from 'framer-motion';

interface BottomSheetProps extends ComponentPropsWithoutRef<'div'> {
  isOpen: boolean;
  onClose?: () => void;
  className?: string;
  backdrop?: boolean;
}

const BottomSheet = ({
  isOpen,
  onClose = () => {},
  children,
  className,
  backdrop = true,
}: BottomSheetProps) => {
  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };
  return (
    <AnimatePresence>
      {isOpen && (
        /* 1. Backdrop (부모) */
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={handleBackdropClick}
          // backdrop이 false면 배경색을 투명하게 처리합니다.
          className={clsx(
            'z-overlay fixed inset-0 flex items-end justify-center',
            backdrop ? 'bg-dimmer-strong' : 'bg-transparent',
          )}
        >
          {/* 2. Content (자식) */}
          <motion.div
            initial={{ y: '100%' }}
            animate={{ y: 0 }}
            exit={{ y: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            // 부모의 클릭 이벤트가 자식에게 전달되지 않도록 방지
            onClick={(e) => e.stopPropagation()}
            className={clsx(
              'bg-background-normal flex h-[80vh] w-full max-w-[calc(100%-20vh)] flex-col rounded-t-2xl px-2 xl:max-w-[calc(100%-30vh)]',
              className,
            )}
          >
            {/* Handle bar */}
            <div className="bg-cool-neutral-95 mx-auto my-2 h-1.5 w-12 shrink-0 rounded-full" />
            {children}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default BottomSheet;
