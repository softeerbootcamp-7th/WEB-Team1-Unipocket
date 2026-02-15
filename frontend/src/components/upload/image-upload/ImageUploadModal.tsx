import Modal from '@/components/modal/Modal';
import UploadBox from '@/components/upload/upload-box/UploadBox';

interface ImageUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const ImageUploadModal = ({ isOpen, onClose }: ImageUploadModalProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      onAction={() => {}}
      className="px-8 pb-4"
    >
      <div className="flex w-242 flex-col gap-6">
        <div className="flex flex-col">
          <div className="flex flex-col gap-3">
            <h2 className="headline1-bold text-label-normal">
              영수증 / 은행 앱 사진 업로드
            </h2>
            <span className="body1-normal-medium text-label-alternative">
              종이 영수증이나 모바일 결제 화면 캡처본을 업로드해 주세요.
            </span>
          </div>
        </div>
        <div className="mb-2.5 flex h-121.5 items-center justify-center">
          <UploadBox type="image" />
        </div>
      </div>
    </Modal>
  );
};

export default ImageUploadModal;
