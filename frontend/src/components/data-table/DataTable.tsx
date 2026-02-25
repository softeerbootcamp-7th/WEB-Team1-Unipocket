import React, { useCallback, useMemo } from 'react';
import { type Cell, flexRender, type Row } from '@tanstack/react-table';
import clsx from 'clsx';

import { useDataTable } from '@/components/data-table/context';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

interface DataTableProps<TData> {
  groupBy?: (row: TData) => string;
  groupDisplay?: (groupKey: string) => string;
  enableGroupSelection?: boolean;
  blankFallbackText?: string;
  getRowIssue?: (row: TData) => boolean;
}

const DataTable = <TData,>({
  groupBy,
  groupDisplay,
  enableGroupSelection = true,
  blankFallbackText,
  getRowIssue,
}: DataTableProps<TData>) => {
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
      const columnId = cell.column.id;
      if (columnId === 'select') return;

      const rect = currentTarget.getBoundingClientRect();
      const payload = {
        rowId: cell.row.id,
        columnId,
        rect,
        value: cell.getValue(),
      };

      const editorType = cell.column.columnDef.meta?.cellEditor;

      if (editorType) {
        switch (editorType) {
          case 'text':
            dispatch({ type: 'SET_TEXT_CELL', payload });
            break;
          case 'category':
            dispatch({ type: 'SET_CATEGORY_CELL', payload });
            break;
          case 'amount':
            dispatch({ type: 'SET_AMOUNT_CELL', payload });
            break;
          case 'method':
            dispatch({ type: 'SET_METHOD_CELL', payload });
            break;
          case 'travel':
            dispatch({ type: 'SET_TRAVEL_CELL', payload });
            break;
          default:
            break;
        }
        return;
      }

      const hasIssue = getRowIssue ? getRowIssue(cell.row.original) : false;

      if (hasIssue) {
        dispatch({ type: 'SET_WARNING_CELL', payload });
      }
    },
    [dispatch, getRowIssue],
  );

  return (
    <Table>
      <TableHeader>
        {table.getHeaderGroups().map((headerGroup) => (
          <TableRow key={headerGroup.id}>
            {headerGroup.headers.map((header) => {
              const isSelectColumn = header.column.id === 'select';
              const columnWidth = isSelectColumn
                ? '48px'
                : `${header.column.getSize()}%`;

              return (
                <TableHead key={header.id} style={{ width: columnWidth }}>
                  {header.isPlaceholder
                    ? null
                    : flexRender(
                        header.column.columnDef.header,
                        header.getContext(),
                      )}
                </TableHead>
              );
            })}
          </TableRow>
        ))}
      </TableHeader>
      <TableBody>
        {Object.keys(groupedRows).length > 0 ? (
          Object.entries(groupedRows).map(([groupKey, dateRows]) => {
            // 2. 해당 그룹의 선택 상태 계산
            const isAllGroupSelected = dateRows.every((row) =>
              row.getIsSelected(),
            );
            const isSomeGroupSelected =
              dateRows.some((row) => row.getIsSelected()) &&
              !isAllGroupSelected;

            const displayLabel = groupDisplay
              ? groupDisplay(groupKey)
              : groupKey;

            return (
              <React.Fragment key={groupKey}>
                {/* 날짜 그룹 헤더 행 */}
                <TableRow
                  data-group-header
                  className="z-grouped-header sticky top-10 border-none bg-white"
                >
                  {enableGroupSelection && (
                    <TableCell className="w-12.5 px-3 py-4">
                      {/* 3. 그룹 선택 체크박스 */}
                      <Checkbox
                        checked={
                          isAllGroupSelected ||
                          (isSomeGroupSelected && 'indeterminate')
                        }
                        onCheckedChange={(value) => {
                          dateRows.forEach((row) =>
                            row.toggleSelected(!!value),
                          );
                        }}
                        aria-label={`Select all rows for ${displayLabel}`}
                      />
                    </TableCell>
                  )}
                  <TableCell
                    colSpan={table.getVisibleLeafColumns().length - 1}
                    className="figure-body2-14-semibold text-label-alternative px-3 py-4"
                  >
                    {displayLabel}
                  </TableCell>
                </TableRow>

                {/* 실제 데이터 행들 */}
                {dateRows.map((row) => {
                  const hasIssue = getRowIssue
                    ? getRowIssue(row.original)
                    : false;
                  return (
                    <TableRow
                      key={row.id}
                      data-state={row.getIsSelected() && 'selected'}
                      className={hasIssue ? 'bg-red-50 hover:bg-red-100' : ''}
                    >
                      {row.getVisibleCells().map((cell, index) => {
                        // 첫 번째 셀(체크박스)이면서 그룹화가 되어있을 때 들여쓰기(pl-11) 적용
                        const isSecondCell = index === 1;
                        const shouldIndent = groupBy && isSecondCell;

                        const isEditable =
                          !!cell.column.columnDef.meta?.cellEditor;
                        return (
                          <TableCell
                            key={cell.id}
                            onClick={(e) =>
                              handleCellClick(cell, e.currentTarget)
                            }
                            // 두 번째 셀일 때만 좌측 패딩(pl-8) 추가
                            className={clsx(
                              shouldIndent ? 'pl-8' : '',
                              isEditable ? 'cursor-pointer' : '',
                            )}
                          >
                            {flexRender(
                              cell.column.columnDef.cell,
                              cell.getContext(),
                            )}
                          </TableCell>
                        );
                      })}
                    </TableRow>
                  );
                })}
              </React.Fragment>
            );
          })
        ) : (
          <TableRow className="hover:bg-background-normal!">
            <TableCell
              colSpan={table.getVisibleLeafColumns().length}
              className="text-center"
            >
              <h1 className="headline1-medium text-label-alternative">
                {blankFallbackText || '데이터가 없습니다'}
              </h1>
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  );
};

export { DataTable };
