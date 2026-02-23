import useEscapeKey from '@/components/upload/hooks/useEscapeKey';

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
  useEscapeKey(isOpen, onClose);

  if (!isOpen) return null;

  return (
    <div
      className="bg-dimmer-strong/60 z-overlay fixed inset-0 flex items-center justify-center"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
    >
      <img
        src={imageUrl}
        alt={imageName}
        className="max-h-[70vh] max-w-[70vw] rounded-lg"
        onClick={(e) => e.stopPropagation()}
      />
    </div>
  );
};

export default ImagePreviewModal;
