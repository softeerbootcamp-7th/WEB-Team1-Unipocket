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

interface TabProviderProps extends Omit<
  ComponentPropsWithoutRef<'div'>,
  'defaultValue'
> {
  defaultValue?: string;
  value?: string;
  onValueChange?: (value: string) => void;
  variant?: TabVariant;
}

const TabProvider = ({
  children,
  defaultValue = '',
  value: controlledValue,
  onValueChange,
  variant = 'underline',
}: TabProviderProps) => {
  // 1. 내부 비제어 상태 (value가 없을 때 사용됨)
  const [uncontrolledValue, setUncontrolledValue] = useState(defaultValue);

  // 2. 제어 모드인지 판단 (외부에서 value prop을 넘겼는가?)
  const isControlled = controlledValue !== undefined;

  // 3. 현재 렌더링할 최종 value 결정
  const currentValue = isControlled ? controlledValue : uncontrolledValue;

  // 4. 상태 변경 함수 (제어/비제어 분기 처리)
  const handleValueChange = (newValue: string) => {
    // 비제어 모드일 때만 내부 상태를 업데이트합니다.
    if (!isControlled) {
      setUncontrolledValue(newValue);
    }
    // 제어/비제어 상관없이 변경 콜백이 있으면 무조건 실행해 줍니다.
    onValueChange?.(newValue);
  };

  return (
    <TabContext.Provider
      value={{
        currentValue,
        setCurrentValue: handleValueChange,
        variant,
      }}
    >
      <div>{children}</div>
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
