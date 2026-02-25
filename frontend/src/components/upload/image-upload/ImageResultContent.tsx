import {
  TabContent,
  TabList,
  TabProvider,
  TabTrigger,
} from '@/components/common/Tab';
import FileImage from '@/components/upload/image-upload/FileImage';
import ImageResultTable from '@/components/upload/image-upload/ImageResultTable';

import type { TempExpenseFile } from '@/api/temporary-expenses/type';

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
              const { successCount, warningCount } = file.expenses.reduce(
                (acc, e) => {
                  if (e.status === 'NORMAL') {
                    acc.successCount++;
                  } else {
                    acc.warningCount++;
                  }
                  return acc;
                },
                { successCount: 0, warningCount: 0 },
              );

              return (
                <TabTrigger
                  key={file.fileId}
                  value={String(file.fileId)}
                  className="w-full"
                >
                  <FileImage
                    accountBookId={accountBookId}
                    metaId={metaId}
                    fileId={file.fileId}
                    fileName={file.fileName}
                    successCount={successCount}
                    warningCount={warningCount}
                    hasIssue={warningCount > 0}
                    variant="tab"
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
                    variant="preview"
                  />
                  <ImageResultTable data={file.expenses} />
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
