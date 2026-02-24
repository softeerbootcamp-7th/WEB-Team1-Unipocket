import FileResultTable from '@/components/upload/file-upload/FileResultTable';

import type { TempExpenseFile } from '@/api/temporary-expenses/type';

interface FileResultContentProps {
  file: TempExpenseFile | null;
}

const FileResultContent = ({ file }: FileResultContentProps) => {
  if (!file) {
    return (
      <div className="flex h-full items-center justify-center">
        <span className="body1-normal-medium text-label-alternative">
          파일을 불러오는 중이에요.
        </span>
      </div>
    );
  }

  return <FileResultTable data={file.expenses} />;
};

export default FileResultContent;
