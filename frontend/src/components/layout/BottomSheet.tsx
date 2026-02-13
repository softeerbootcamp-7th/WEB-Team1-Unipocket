import type { ComponentPropsWithoutRef } from 'react';
import { AnimatePresence, motion } from 'framer-motion';

import { cn } from '@/lib/utils';

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
  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {backdrop && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={onClose}
              className="bg-dimmer-strong fixed inset-0 z-40"
            />
          )}
          <motion.div
            initial={{ y: '100%' }}
            animate={{ y: 0 }}
            exit={{ y: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className={cn(
              `bg-background-normal fixed right-[12vh] bottom-0 left-[18vh] z-50 h-[80vh] rounded-t-2xl ${className}`,
            )}
          >
            <div className="bg-cool-neutral-95 mx-auto my-3 h-1.5 w-12 rounded-full" />
            {children}
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

export default BottomSheet;
