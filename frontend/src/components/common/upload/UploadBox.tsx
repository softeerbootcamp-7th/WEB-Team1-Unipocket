import { Icons } from '@/assets';

const UploadBox = () => {
  return (
    <div className="bg-background-alternative flex items-center justify-center py-20">
      <Icons.UploadFile className="text-label-alternative mr-4 h-12 w-12" />
      <div className="text-label-alternative">
        <h3 className="heading2-bold">
          클릭하거나 드래그해서 이미지를 업로드해주세요
        </h3>
        <h3 className="headline1-medium">
          jpg, jpeg, png, heic (최대 3개, 총 20MB 이하)
        </h3>
      </div>
    </div>
  );
};

export default UploadBox;
