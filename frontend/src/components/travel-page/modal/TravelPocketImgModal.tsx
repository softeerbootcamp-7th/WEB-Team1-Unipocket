import Modal, { type ModalProps } from '@/components/modal/Modal';

interface TravelPocketImgModalProps extends Omit<
  ModalProps,
  'children' | 'onAction'
> {
  travelId: number;
  imageUrl: string | null;
  onAction?: () => void;
}

const TravelPocketImgModal = ({
  travelId,
  imageUrl,
  onAction,
  onClose,
  ...modalProps
}: TravelPocketImgModalProps) => {
  const handleAction = () => {
    onAction?.();
    onClose();
  };

  return (
    <Modal {...modalProps} onClose={onClose} onAction={handleAction}>
      <div className="flex flex-col items-center gap-4 p-4">
        <p className="headline1-bold text-label-normal">썸네일 변경</p>
        <p className="body1-normal-medium text-label-alternative">
          {travelId}UI는 추후 구현 예정
        </p>
        {imageUrl && (
          <img
            src={imageUrl}
            alt="현재 썸네일"
            className="h-40 w-40 rounded-lg object-cover"
          />
        )}
      </div>
    </Modal>
  );
};

export default TravelPocketImgModal;
