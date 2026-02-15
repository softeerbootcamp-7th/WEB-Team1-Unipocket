import type { ComponentPropsWithoutRef } from 'react';
import { AnimatePresence, motion } from 'framer-motion';

interface BottomSheetProps extends ComponentPropsWithoutRef<'div'> {
  isOpen: boolean;
  onClose: () => void;
}

const BottomSheet = ({ isOpen, onClose, children }: BottomSheetProps) => {
  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="bg-dimmer-strong fixed inset-0 z-40"
          />
          <motion.div
            initial={{ y: '100%' }}
            animate={{ y: 0 }}
            exit={{ y: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className="bg-background-normal fixed right-[10vh] bottom-0 left-[10vh] z-50 flex h-[80vh] flex-col rounded-t-2xl px-2 py-4"
          >
            <div className="bg-cool-neutral-30 absolute top-3 left-1/2 h-1.5 w-12 -translate-x-1/2 rounded-full" />
            {children}
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

export default BottomSheet;
