import { useEffect } from 'react';

import { useModalContext } from '@/components/modal/useModalContext';

const ModalFormContent = ({
  isActionReady,
  children,
}: {
  isActionReady: boolean;
  children: React.ReactNode;
}) => {
  const { setActionReady } = useModalContext();

  useEffect(() => {
    setActionReady(isActionReady);
  }, [isActionReady, setActionReady]);

  return <div className="pb-4">{children}</div>;
};

export default ModalFormContent;
