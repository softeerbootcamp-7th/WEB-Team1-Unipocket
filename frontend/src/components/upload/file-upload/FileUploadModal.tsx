import Modal from '@/components/modal/Modal';
import UploadBox from '@/components/upload/upload-box/UploadBox';

interface FileUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const FileUploadModal = ({ isOpen, onClose }: FileUploadModalProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      onAction={() => {}}
      className="px-8 pb-4"
    >
      <div className="flex w-109 flex-col gap-6">
        <div className="flex flex-col">
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
        </div>
        <div className="mb-2.5 flex h-65.5 items-center justify-center">
          <UploadBox type="file" />
        </div>
      </div>
    </Modal>
  );
};

export default FileUploadModal;
