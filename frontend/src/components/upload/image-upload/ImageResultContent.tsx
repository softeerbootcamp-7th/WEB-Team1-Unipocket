import type { ColumnDef } from '@tanstack/react-table';

import {
  TabContent,
  TabList,
  TabProvider,
  TabTrigger,
} from '@/components/common/Tab';
import { DataTable } from '@/components/data-table/DataTable';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import TabImage from '@/components/upload/image-upload/TabImage';

import { CATEGORIES } from '@/types/category';

import { useGetMetaFileUrlQuery } from '@/api/temporary-expenses/query';
import type {
  TempExpense,
  TempExpenseFile,
} from '@/api/temporary-expenses/type';

const tempExpenseColumns: ColumnDef<TempExpense>[] = [
  {
    accessorKey: 'occurredAt',
    header: () => <>날짜</>,
    cell: ({ row }) =>
      new Date(row.original.occurredAt).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      }),
  },
  {
    accessorKey: 'merchantName',
    header: () => <>거래처</>,
  },
  {
    accessorKey: 'category',
    header: () => <>카테고리</>,
    cell: ({ row }) => CATEGORIES[row.original.category]?.name ?? '-',
  },
  {
    id: 'localAmount',
    header: () => <>현지 금액</>,
    cell: ({ row }) =>
      `${row.original.localCurrencyAmount.toLocaleString()} ${row.original.localCountryCode}`,
  },
  {
    id: 'baseAmount',
    header: () => <>원화 금액</>,
    cell: ({ row }) =>
      `${row.original.baseCurrencyAmount.toLocaleString()} ${row.original.baseCountryCode}`,
  },
  {
    accessorKey: 'status',
    header: () => <>상태</>,
  },
];

interface FileImageProps {
  accountBookId: number;
  metaId: number;
  fileId: number;
  fileName: string;
}

interface FileTabImageProps extends FileImageProps {
  successCount: number;
  warningCount: number;
  hasIssue: boolean;
}

const FileTabImage = ({
  accountBookId,
  metaId,
  fileId,
  fileName,
  successCount,
  warningCount,
  hasIssue,
}: FileTabImageProps) => {
  const { data } = useGetMetaFileUrlQuery(accountBookId, metaId, fileId);

  return (
    <TabImage
      thumbnailUrl={data?.presignedUrl ?? ''}
      fileName={fileName}
      successCount={successCount}
      warningCount={warningCount}
      hasIssue={hasIssue}
    />
  );
};

const FileImage = ({
  accountBookId,
  metaId,
  fileId,
  fileName,
}: FileImageProps) => {
  const { data } = useGetMetaFileUrlQuery(accountBookId, metaId, fileId);

  return (
    <div className="bg-background-alternative flex items-center justify-center rounded-2xl border border-gray-200 p-2.5">
      {data?.presignedUrl ? (
        <img
          src={data.presignedUrl}
          alt={fileName}
          className="h-120 rounded-lg"
        />
      ) : (
        <div className="h-120 w-full animate-pulse rounded-lg bg-gray-200" />
      )}
    </div>
  );
};

interface ImageResultContentProps {
  accountBookId: number;
  metaId: number;
  files: TempExpenseFile[];
}

const ImageResultContent = ({
  accountBookId,
  metaId,
  files,
}: ImageResultContentProps) => {
  if (files.length === 0) {
    return (
      <div className="flex h-full items-center justify-center">
        <span className="body1-normal-medium text-label-alternative">
          파일을 불러오는 중이에요.
        </span>
      </div>
    );
  }

  return (
    <div>
      <div className="px-4.25">
        <TabProvider variant="underline" defaultValue={String(files[0].fileId)}>
          <TabList>
            {files.map((file) => {
              const successCount = file.expenses.filter(
                (e) => e.status === 'NORMAL',
              ).length;
              const warningCount = file.expenses.filter(
                (e) => e.status !== 'NORMAL',
              ).length;
              const fileName = file.s3Key.split('/').pop() ?? file.s3Key;

              return (
                <TabTrigger key={file.fileId} value={String(file.fileId)}>
                  <FileTabImage
                    accountBookId={accountBookId}
                    metaId={metaId}
                    fileId={file.fileId}
                    fileName={fileName}
                    successCount={successCount}
                    warningCount={warningCount}
                    hasIssue={warningCount > 0}
                  />
                </TabTrigger>
              );
            })}
          </TabList>
          <div className="h-10" />
          {files.map((file) => {
            const fileName = file.s3Key.split('/').pop() ?? file.s3Key;

            return (
              <TabContent key={file.fileId} value={String(file.fileId)}>
                <div className="flex flex-col gap-4.5 lg:flex-row">
                  <FileImage
                    accountBookId={accountBookId}
                    metaId={metaId}
                    fileId={file.fileId}
                    fileName={fileName}
                  />
                  <div className="shadow-semantic-subtle h-fit min-w-0 flex-1 rounded-2xl px-2 py-4">
                    <DataTableProvider
                      columns={tempExpenseColumns}
                      data={file.expenses}
                    >
                      <DataTable<TempExpense>
                        enableGroupSelection={false}
                        groupBy={(row) =>
                          new Date(row.occurredAt).toLocaleDateString('ko-KR', {
                            year: 'numeric',
                            month: '2-digit',
                            day: '2-digit',
                          })
                        }
                        blankFallbackText="지출 내역이 없습니다"
                      />
                    </DataTableProvider>
                  </div>
                </div>
              </TabContent>
            );
          })}
        </TabProvider>
      </div>
    </div>
  );
};

export default ImageResultContent;
