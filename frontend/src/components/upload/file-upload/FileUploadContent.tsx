import { useEffect } from 'react';

import { useModalContext } from '@/components/modal/useModalContext';
import UploadFile from '@/components/upload/file-upload/UploadFile';
import { useFileUpload } from '@/components/upload/file-upload/useFileUpload';
import UploadBox from '@/components/upload/upload-box/UploadBox';

const FileUploadContent = () => {
  const { setActionReady } = useModalContext();
  const { item, handleFilesSelected, removeItem, isReady } = useFileUpload();

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
        {!item && (
          <UploadBox type="file" onFilesSelected={handleFilesSelected} />
        )}
        {item && <UploadFile key={item.id} item={item} onRemove={removeItem} />}
      </div>
    </div>
  );
};

export default FileUploadContent;
