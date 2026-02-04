import {
  type ComponentPropsWithoutRef,
  createContext,
  useContext,
  useState,
} from 'react';

type TabVariant = 'underline' | 'pills';

const variantMap = {
  underline: {
    active: 'border-b-2 pb-[14px]',
    inactive: 'pb-[14px]',
  },
  pills: {
    active: 'bg-black text-white shadow-sm',
    inactive: 'text-label-alternative hover:bg-background-alternative',
  },
};

interface TabContextType {
  currentValue: string;
  setCurrentValue: (value: string) => void;
  variant: TabVariant;
}

const TabContext = createContext<TabContextType | null>(null);

const useTab = () => {
  const context = useContext(TabContext);
  if (!context) {
    throw new Error('useTab must be used within a TabProvider');
  }
  return context;
};

interface TabProviderProps extends ComponentPropsWithoutRef<'div'> {
  defaultValue: string;
  variant?: TabVariant;
}

const TabProvider = ({
  children,
  defaultValue,
  variant = 'underline',
}: TabProviderProps) => {
  const [currentValue, setCurrentValue] = useState(defaultValue);

  return (
    <TabContext.Provider
      value={{
        currentValue,
        setCurrentValue,
        variant,
      }}
    >
      {children}
    </TabContext.Provider>
  );
};

const TabList = ({ children }: { children: React.ReactNode }) => {
  return <div className="flex gap-6">{children}</div>;
};

interface TabTriggerProps extends ComponentPropsWithoutRef<'button'> {
  value: string;
}

const TabTrigger = ({ value, children }: TabTriggerProps) => {
  const { currentValue, setCurrentValue, variant } = useTab();
  const isActive = currentValue === value;

  const currentVariantStyles = variantMap[variant];
  const stateClass = isActive
    ? currentVariantStyles.active
    : currentVariantStyles.inactive;

  return (
    <button onClick={() => setCurrentValue(value)} className={stateClass}>
      {children}
    </button>
  );
};

interface TabContentProps {
  value: string;
  children: React.ReactNode;
}

const TabContent = ({ value, children }: TabContentProps) => {
  const context = useTab();
  const { currentValue } = context;
  if (currentValue !== value) return null;
  return <>{children}</>;
};

export { TabContent, TabList, TabProvider, TabTrigger };
