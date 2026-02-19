import UploadImage from '@/components/upload/image-upload/UploadImage';
import { type UploadItem } from '@/components/upload/type';

interface UploadGalleryProps {
  items: UploadItem[];
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
