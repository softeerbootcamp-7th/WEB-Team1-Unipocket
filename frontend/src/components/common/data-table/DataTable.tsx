import React, { useCallback, useMemo } from 'react';
import { type Cell, flexRender, type Row } from '@tanstack/react-table';

import { Checkbox } from '@/components/ui/checkbox';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

import { useDataTable } from './context';

interface DataTableProps<TData> {
  groupBy?: (row: TData) => string;
  height: number;
}

const DataTable = <TData,>({ groupBy, height }: DataTableProps<TData>) => {
  const { table, dispatch } = useDataTable();
  const rows = table.getRowModel().rows as Row<TData>[];

  const groupedRows = useMemo(() => {
    return rows.reduce(
      (acc, row) => {
        const key = groupBy ? groupBy(row.original) : 'Ungrouped';

        if (!acc[key]) acc[key] = [];
        acc[key].push(row);
        return acc;
      },
      {} as Record<string, Row<TData>[]>,
    );
  }, [rows, groupBy]);

  const handleCellClick = useCallback(
    (
      cell: Cell<TData, unknown>,
      currentTarget: EventTarget & HTMLTableCellElement,
    ) => {
      const rect = currentTarget.getBoundingClientRect();

      dispatch({
        type: 'SET_ACTIVE_CELL',
        payload: {
          rowId: cell.row.id,
          columnId: cell.column.id,
          rect,
          value: cell.getValue(),
        },
      });
    },
    [dispatch],
  ); // dispatch가 바뀌지 않는 한 함수 재사용

  return (
    <Table height={height}>
      <TableHeader className="sticky top-0 z-10">
        {table.getHeaderGroups().map((headerGroup) => (
          <TableRow key={headerGroup.id}>
            {headerGroup.headers.map((header) => (
              <TableHead key={header.id}>
                {header.isPlaceholder
                  ? null
                  : flexRender(
                      header.column.columnDef.header,
                      header.getContext(),
                    )}
              </TableHead>
            ))}
          </TableRow>
        ))}
      </TableHeader>
      <TableBody>
        {Object.keys(groupedRows).length > 0 ? (
          Object.entries(groupedRows).map(([date, dateRows]) => {
            // 2. 해당 그룹의 선택 상태 계산
            const isAllGroupSelected = dateRows.every((row) =>
              row.getIsSelected(),
            );
            const isSomeGroupSelected =
              dateRows.some((row) => row.getIsSelected()) &&
              !isAllGroupSelected;

            return (
              <React.Fragment key={date}>
                {/* 날짜 그룹 헤더 행 */}
                <TableRow
                  data-group-header
                  className="sticky top-10 z-10 border-none bg-white"
                >
                  <TableCell className="w-12.5 px-3 py-4">
                    {/* 3. 그룹 선택 체크박스 */}
                    <Checkbox
                      checked={
                        isAllGroupSelected ||
                        (isSomeGroupSelected && 'indeterminate')
                      }
                      onCheckedChange={(value) => {
                        dateRows.forEach((row) => row.toggleSelected(!!value));
                      }}
                      aria-label={`Select all rows for ${date}`}
                    />
                  </TableCell>
                  <TableCell
                    colSpan={table.getVisibleLeafColumns().length - 1}
                    className="figure-body2-14-semibold text-label-alternative px-3 py-4"
                  >
                    {date}
                  </TableCell>
                </TableRow>

                {/* 실제 데이터 행들 */}
                {dateRows.map((row) => (
                  <TableRow
                    key={row.id}
                    data-state={row.getIsSelected() && 'selected'}
                  >
                    {row.getVisibleCells().map((cell) => {
                      const isSelectColumn = cell.column.id === 'select';
                      return (
                        <TableCell
                          key={cell.id}
                          onClick={
                            isSelectColumn
                              ? undefined
                              : (e) => handleCellClick(cell, e.currentTarget)
                          }
                          className={isSelectColumn ? '' : 'cursor-pointer'}
                        >
                          {flexRender(
                            cell.column.columnDef.cell,
                            cell.getContext(),
                          )}
                        </TableCell>
                      );
                    })}
                  </TableRow>
                ))}
              </React.Fragment>
            );
          })
        ) : (
          <TableRow>
            <TableCell
              colSpan={table.getVisibleLeafColumns().length}
              className="h-24 text-center"
            >
              No results.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  );
};

export { DataTable };
