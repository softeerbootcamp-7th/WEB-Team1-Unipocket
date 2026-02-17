interface ImagePreviewModalProps {
  isOpen: boolean;
  imageUrl: string;
  imageName: string;
  onClose: () => void;
}

const ImagePreviewModal = ({
  isOpen,
  imageUrl,
  imageName,
  onClose,
}: ImagePreviewModalProps) => {
  if (!isOpen) return null;

  return (
    <div
      className="bg-dimmer-strong/60 fixed inset-0 z-100 flex items-center justify-center"
      onClick={onClose}
    >
      <img
        src={imageUrl}
        alt={imageName}
        className="max-h-[80vh] max-w-[80vw] rounded-lg"
        onClick={(e) => e.stopPropagation()}
      />
    </div>
  );
};

export default ImagePreviewModal;
