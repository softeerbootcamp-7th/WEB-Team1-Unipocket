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
    <div className="flex h-full min-h-0 flex-col px-4.25">
      <TabProvider
        variant="underline"
        defaultValue={String(files[0].fileId)}
        className="flex h-full min-h-0 flex-col"
      >
        <TabList>
          {files.map((file) => {
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
                  successCount={file.normalCount + file.abnormalCount}
                  warningCount={file.incompleteCount}
                  variant="tab"
                />
              </TabTrigger>
            );
          })}
        </TabList>
        <div className="h-10 shrink-0" />
        {files.map((file) => {
          return (
            <TabContent key={file.fileId} value={String(file.fileId)}>
              <div className="flex min-h-0 flex-1 flex-col gap-4.5 lg:flex-row">
                <FileImage
                  accountBookId={accountBookId}
                  metaId={metaId}
                  fileId={file.fileId}
                  fileName={file.fileName}
                  variant="preview"
                />
                <ImageResultTable data={file.expenses} />
              </div>
            </TabContent>
          );
        })}
      </TabProvider>
    </div>
  );
};

export default ImageResultContent;
