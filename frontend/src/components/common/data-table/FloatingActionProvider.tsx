import { useDataTable } from "./context";

const FloatingActionProvider = () => {
  const { table, tableState, dispatch } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;

  if (tableState.floatingBarVariant === "NONE") return null;

  return (
    <div className="fixed bottom-10 left-1/2 -translate-x-1/2 bg-white shadow-2xl border rounded-lg p-4">
      {tableState.floatingBarVariant === "MANAGEMENT" ? (
        // 1번 바: 카테고리/결제수단/여행/숨김
        <div className="flex gap-4">
          <button
            onClick={() => {
              /* 카테고리 변경 로직 */
            }}
          >
            카테고리
          </button>
          <button
            onClick={() => {
              /* 결제수단 변경 로직 */
            }}
          >
            결제수단
          </button>
          <button>숨김</button>
        </div>
      ) : (
        // 2번 바: 현재 가계부에 추가하기
        <div className="flex gap-4">
          <span>{selectedRows.length}개 선택됨</span>
          <button
            onClick={() =>
              dispatch({ type: "SET_BAR_VARIANT", payload: "ADD_TO_LEDGER" })
            }
          >
            가계부에 추가하기
          </button>
        </div>
      )}
    </div>
  );
};

export default FloatingActionProvider;
