import { columns } from "./payments/columns";
import { DataTable } from "./components/data-table/DataTable";
import { getData } from "./payments/page";

function App() {
  const data = getData();
  return (
    <div className="container mx-auto py-10">
      <DataTable columns={columns} data={data} />
    </div>
  );
}

export default App;
