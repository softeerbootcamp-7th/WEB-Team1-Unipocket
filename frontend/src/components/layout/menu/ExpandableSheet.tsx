import type { ComponentPropsWithoutRef } from 'react';
import { AnimatePresence, motion, type PanInfo } from 'framer-motion';

const SWIPE_THRESHOLD = 50;

interface ExpandableSheetProps extends ComponentPropsWithoutRef<'div'> {
  isExpanded: boolean;
  onToggleExpand: (isExpanded: boolean) => void;
  collapsedHeight: string | number;
  expandedHeight: string | number;
}

const ExpandableSheet = ({
  isExpanded,
  onToggleExpand,
  collapsedHeight = '50vh',
  expandedHeight = '90vh',
  children,
}: ExpandableSheetProps) => {
  const handlePanEnd = (
    _: MouseEvent | TouchEvent | PointerEvent,
    info: PanInfo,
  ) => {
    if (info.offset.y < -SWIPE_THRESHOLD) {
      if (!isExpanded) onToggleExpand(true);
    } else if (info.offset.y > SWIPE_THRESHOLD) {
      if (isExpanded) {
        onToggleExpand(false);
      } else {
        onToggleExpand(false);
      }
    }
  };
  return (
    <AnimatePresence>
      <motion.div
        layout
        initial={{ y: '100%' }}
        animate={{
          y: 0,
          height: isExpanded ? expandedHeight : collapsedHeight,
        }}
        exit={{ y: '100%' }}
        transition={{ type: 'spring', damping: 25, stiffness: 200 }}
        className="bg-background-normal absolute bottom-0 flex flex-col overflow-hidden rounded-t-2xl"
      >
        <motion.div
          onPanEnd={handlePanEnd}
          className="relative flex w-full cursor-grab touch-none justify-center py-4 select-none active:cursor-grabbing"
        >
          <div className="bg-cool-neutral-95 absolute top-3 h-1.5 w-12 shrink-0 rounded-full" />
        </motion.div>
        <div className="flex-1 overflow-y-auto px-2 pb-4">{children}</div>
      </motion.div>
    </AnimatePresence>
  );
};

export default ExpandableSheet;
