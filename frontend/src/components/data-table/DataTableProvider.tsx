import {
  createContext,
  useContext,
  useState,
  type Dispatch,
  type SetStateAction,
} from "react";

const NewsStandTabContext = createContext<NewsStandTabContextType | null>(null);

interface NewsStandTabContextType {
  activeTab: TabValue;
  setActiveTab: Dispatch<SetStateAction<TabValue>>;
}

const DataTableProvider = ({ children }: { children: React.ReactNode }) => {
  const [activeTab, setActiveTab] = useState<TabValue>(TAB_VALUES.ALL);
  return (
    <NewsStandTabContext.Provider value={{ activeTab, setActiveTab }}>
      {children}
    </NewsStandTabContext.Provider>
  );
};

export default DataTableProvider;

// eslint-disable-next-line react-refresh/only-export-components
export const useNewsStandTab = () => {
  const context = useContext(NewsStandTabContext);
  if (!context) {
    throw new Error("useNewsStandTab must be used within a DataTableProvider");
  }
  return context;
};
