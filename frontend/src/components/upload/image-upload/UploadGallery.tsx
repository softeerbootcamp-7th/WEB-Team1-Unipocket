import UploadImage, {
  type UploadImageItem,
} from '@/components/upload/image-upload/UploadImage';

interface UploadGalleryProps {
  items: UploadImageItem[];
  onRemove: (id: string) => void;
}

const UploadGallery = ({ items, onRemove }: UploadGalleryProps) => {
  return (
    <div className="flex gap-2.5">
      {items.map((item) => (
        <UploadImage key={item.id} item={item} onRemove={onRemove} />
      ))}
    </div>
  );
};

export default UploadGallery;
