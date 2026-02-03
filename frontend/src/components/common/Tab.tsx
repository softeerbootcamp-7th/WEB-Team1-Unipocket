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
    inactive: 'text-gray-500 hover:bg-gray-100',
  },
};

interface TabContextType {
  value: string;
  setValue: (value: string) => void;
  variant: TabVariant;
  setVariant: (variant: TabVariant) => void;
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
  const [value, setValue] = useState(defaultValue);
  const [currentVariant, setVariant] = useState(variant);

  return (
    <TabContext.Provider
      value={{ value, setValue, variant: currentVariant, setVariant }}
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
  const { value: activeValue, setValue, variant } = useTab();
  const isActive = activeValue === value;

  const currentVariant = variantMap[variant];
  const stateClass = isActive ? currentVariant.active : currentVariant.inactive;

  return (
    <button onClick={() => setValue(value)} className={stateClass}>
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
  const { value: activeValue } = context;
  if (activeValue !== value) return null;
  return <>{children}</>;
};

export { TabContent, TabList, TabProvider, TabTrigger };
