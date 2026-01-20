import { columns, type Payment } from "./columns";
import { DataTable } from "../components/data-table/DataTable";

export function getData(): Payment[] {
  // Fetch data from your API here.
  return [
    {
      id: "1",
      date: "2025.12.02",
      merchant: "Coles (Wollongong Central) Groceries",
      category: "생활",
      localCurrency: "USD",
      localAmount: 20,
      baseAmount: 29414,
      exchangeRate: 1471,
      paymentMethod: "Chase Bank",
    },
    {
      id: "2",
      date: "2025.12.02",
      merchant: "김이람",
      category: "수입",
      localCurrency: "USD",
      localAmount: 65646.9,
      baseAmount: 65.9,
      exchangeRate: 1455,
      paymentMethod: "하나 비바 X",
    },
    {
      id: "3",
      date: "2025.12.02",
      merchant: "스픽",
      category: "생활",
      localCurrency: "KRW",
      localAmount: 9417,
      baseAmount: 9417,
      exchangeRate: 1,
      paymentMethod: "하나 비바 X",
    },
    {
      id: "4",
      date: "2025.12.02",
      merchant: "Coles (Wollongong Central) Groceries",
      category: "생활",
      localCurrency: "CAD",
      localAmount: 421.12,
      baseAmount: 29414,
      exchangeRate: 1471,
      paymentMethod: "하나 비바 X",
    },
    {
      id: "5",
      date: "2025.12.02",
      merchant: "AIRBNB",
      category: "거주",
      localCurrency: "USD",
      localAmount: 129.99,
      baseAmount: 191175123,
      exchangeRate: 1471,
      paymentMethod: "하나 비바 X",
      trip: "뉴욕",
    },
    {
      id: "6",
      date: "2025.12.01",
      merchant: "AIRBNB",
      category: "거주",
      localCurrency: "USD",
      localAmount: 213.11,
      baseAmount: 191175,
      exchangeRate: 1431,
      paymentMethod: "현금",
      trip: "뉴욕",
    },
    {
      id: "7",
      date: "2025.12.01",
      merchant: "AIRBNB",
      category: "거주",
      localCurrency: "USD",
      localAmount: 213.11,
      baseAmount: 191175,
      exchangeRate: 1431,
      paymentMethod: "Chase Bank",
      trip: "뉴욕",
    },
  ];
}

export default function DemoPage() {
  const data = getData();

  return (
    <div className="container mx-auto py-10">
      <DataTable columns={columns} data={data} />
    </div>
  );
}
