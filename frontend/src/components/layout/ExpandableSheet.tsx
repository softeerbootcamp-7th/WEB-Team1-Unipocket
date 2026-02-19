import type { ComponentPropsWithoutRef } from 'react';
import { AnimatePresence, motion, type PanInfo } from 'framer-motion';

const SWIPE_THRESHOLD = 50;

interface ExpandableSheetProps extends ComponentPropsWithoutRef<'div'> {
  isExpandable?: boolean;
  isExpanded?: boolean;
  onToggleExpand?: (isExpanded: boolean) => void;
  collapsedHeight: string | number;
  expandedHeight?: string | number;
}

const ExpandableSheet = ({
  isExpanded,
  onToggleExpand,
  collapsedHeight = '50vh',
  expandedHeight = '90vh',
  isExpandable = true,
  children,
}: ExpandableSheetProps) => {
  const handlePanEnd = (
    _: MouseEvent | TouchEvent | PointerEvent,
    info: PanInfo,
  ) => {
    if (info.offset.y < -SWIPE_THRESHOLD) {
      if (!isExpanded && onToggleExpand) onToggleExpand(true);
    } else if (info.offset.y > SWIPE_THRESHOLD) {
      if (onToggleExpand) onToggleExpand(false);
    }
  };
  return (
    <AnimatePresence>
      <motion.div
        initial={{ height: 0 }}
        animate={{
          height: isExpanded ? expandedHeight : collapsedHeight,
        }}
        exit={{ height: 0 }}
        transition={{
          type: 'tween',
          ease: [0.25, 1, 0.5, 1],
          duration: 0.4,
        }}
        className="bg-background-normal absolute inset-x-0 bottom-0 flex flex-col overflow-hidden rounded-t-2xl"
      >
        {isExpandable && (
          <motion.div
            onPanEnd={handlePanEnd}
            className="relative flex cursor-grab touch-none justify-center py-4 select-none active:cursor-grabbing"
          >
            <div className="bg-cool-neutral-95 absolute top-3 h-1.5 w-12 shrink-0 rounded-full" />
          </motion.div>
        )}
        <div className="flex min-h-0 flex-1 flex-col px-2 pb-4">{children}</div>
      </motion.div>
    </AnimatePresence>
  );
};

export default ExpandableSheet;
