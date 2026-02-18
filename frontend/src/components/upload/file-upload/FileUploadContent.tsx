import { useEffect, useState } from 'react';

import { useModalContext } from '@/components/modal/useModalContext';
import UploadFile from '@/components/upload/file-upload/UploadFile';
import UploadBox from '@/components/upload/upload-box/UploadBox';

const FileUploadContent = () => {
  const { setActionReady } = useModalContext();

  const [file, setFile] = useState<File | null>(null);
  const [status, setStatus] = useState<'uploading' | 'done'>('uploading');

  const handleFilesSelected = (selected: File[]) => {
    if (selected.length !== 1) return;
    const file = selected[0];
    setFile(file);
    setStatus('uploading');

    // @TODO: 업로드 로직 구현 후 테스트 코드 제거
    // 테스트용: 2초 뒤 done 처리
    setTimeout(() => {
      setStatus('done');
    }, 2000);
  };

  const handleRemove = () => {
    setFile(null);
    setStatus('uploading');
  };

  const isReady = !!file && status === 'done';

  useEffect(() => {
    setActionReady(isReady);
  }, [isReady, setActionReady]);

  return (
    <div className="flex w-109 flex-col gap-6">
      <div className="flex flex-col gap-3">
        <h2 className="headline1-bold text-label-normal">
          거래 내역 파일 업로드
        </h2>
        <span className="body1-normal-medium text-label-alternative">
          은행 앱이나 카드사 홈페이지에서 다운로드한 거래 내역 파일을
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
            name={file.name}
            status={status}
            onDelete={handleRemove}
          />
        )}
      </div>
    </div>
  );
};

export default FileUploadContent;
