import { useEffect } from 'react';

import { useModalContext } from '@/components/modal/useModalContext';
import UploadFile from '@/components/upload/file-upload/UploadFile';
import type { UploadItem } from '@/components/upload/type';
import UploadBox from '@/components/upload/upload-box/UploadBox';

interface FileUploadContentProps {
  item: UploadItem | null;
  onFilesSelected: (files: File[]) => void;
  onRemove: () => void;
  isReady: boolean;
}

const FileUploadContent = ({
  item,
  onFilesSelected,
  onRemove,
  isReady,
}: FileUploadContentProps) => {
  const { setActionReady } = useModalContext();

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
        {!item && <UploadBox type="file" onFilesSelected={onFilesSelected} />}
        {item && <UploadFile key={item.id} item={item} onRemove={onRemove} />}
      </div>
    </div>
  );
};

export default FileUploadContent;
