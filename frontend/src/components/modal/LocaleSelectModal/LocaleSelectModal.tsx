import { AnimatePresence, motion } from 'framer-motion';

import { useEscapeKey } from '@/hooks/useKeyboardEvent';

import LocaleSelectContent from '@/components/modal/LocaleSelectModal/LocaleSelectContent';

import type { CountryCode } from '@/data/country/countryCode';

export type LocaleMode = 'BASE' | 'LOCAL' | 'INIT';

interface LocaleSelectModalProps {
  isOpen: boolean;
  onClose: () => void;
  mode: LocaleMode;
  onSelect?: (code: CountryCode) => void;
  baseCountryCode: CountryCode | null;
  localCountryCode: CountryCode | null;
}

const LocaleSelectModal = ({
  isOpen,
  onClose,
  ...contentProps
}: LocaleSelectModalProps) => {
  useEscapeKey(isOpen, onClose);

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          className="bg-dimmer-strong z-overlay fixed inset-0 box-border flex h-dvh w-full justify-center pt-12"
          onClick={onClose}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.2 }}
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            transition={{ duration: 0.2 }}
            onClick={(e) => e.stopPropagation()}
          >
            <LocaleSelectContent {...contentProps} />
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default LocaleSelectModal;
