import { useContext, useEffect, useState } from 'react';

import { ModalContext } from '@/components/modal/useModalContext';
import UploadFile from '@/components/upload/file-upload/UploadFile';
import UploadBox from '@/components/upload/upload-box/UploadBox';

const FileUploadContent = () => {
  const context = useContext(ModalContext);

  const [file, setFile] = useState<File | null>(null);

  const handleFilesSelected = (selected: File[]) => {
    if (selected.length === 0) return;
    setFile(selected[0]);
  };

  const handleRemove = () => {
    setFile(null);
  };

  const isReady = !!file;

  useEffect(() => {
    context?.setActionReady(isReady);
  }, [isReady, context]);

  return (
    <div className="flex w-109 flex-col gap-6">
      <div className="flex flex-col gap-3">
        <h2 className="headline1-bold text-label-normal">
          거래 내역 파일 업로드
        </h2>
        <span className="body1-normal-medium text-label-alternative">
          은행 앱이나 카드사 홈페이지에서 다운로드한 엑셀, PDF 내역서를
          <br />
          업로드해 주세요.
        </span>
      </div>
      <div className="mb-2.5 h-65.5">
        {!file && (
          <UploadBox type="file" onFilesSelected={handleFilesSelected} />
        )}
        {file && (
          <UploadFile
            fileName={file.name}
            status="uploading" // @TODO: 실제 업로드 상태 연결
            onDelete={handleRemove}
          />
        )}
      </div>
    </div>
  );
};

export default FileUploadContent;
