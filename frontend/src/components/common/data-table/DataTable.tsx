import React from 'react';
import { flexRender } from '@tanstack/react-table';

import { Checkbox } from '@/components/ui/checkbox'; // 체크박스 컴포넌트 임포트
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

import { useDataTable } from './context';

const DataTable = () => {
  const { table } = useDataTable();
  const rows = table.getRowModel().rows;

  // 1. 데이터를 날짜별로 그룹화
  const groupedRows = rows.reduce(
    (acc, row) => {
      const date = new Date(row.original.date).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      });
      if (!acc[date]) acc[date] = [];
      acc[date].push(row);
      return acc;
    },
    {} as Record<string, typeof rows>,
  );

  return (
    <div className="overflow-hidden rounded-md">
      <Table>
        <TableHeader>
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
                  <TableRow className="border-none bg-transparent hover:bg-transparent">
                    <TableCell className="w-[50px] px-3 py-4">
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
                        aria-label={`Select all rows for ${date}`}
                      />
                    </TableCell>
                    <TableCell
                      colSpan={table.getVisibleLeafColumns().length - 1}
                      className="px-3 py-4 text-sm font-semibold text-gray-500"
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
                      {row.getVisibleCells().map((cell) => (
                        <TableCell key={cell.id}>
                          {flexRender(
                            cell.column.columnDef.cell,
                            cell.getContext(),
                          )}
                        </TableCell>
                      ))}
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
    </div>
  );
};

export { DataTable };
