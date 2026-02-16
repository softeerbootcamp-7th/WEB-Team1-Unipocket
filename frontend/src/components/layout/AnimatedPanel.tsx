import { AnimatePresence, motion } from 'framer-motion';

import { cn } from '@/lib/utils';

interface AnimatedPanelProps {
  isOpen: boolean;
  className?: string;
  children?: React.ReactNode;
}

const AnimatedPanel = ({ isOpen, children, className }: AnimatedPanelProps) => {
  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ y: '100%' }}
          animate={{ y: 0 }}
          exit={{ y: '100%' }}
          transition={{ type: 'spring', damping: 25, stiffness: 200 }}
          className={cn(
            'bg-background-normal flex h-full w-full flex-col overflow-hidden rounded-2xl',
            className,
          )}
        >
          {children}
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default AnimatedPanel;
