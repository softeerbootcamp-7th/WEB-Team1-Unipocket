import { Icons } from '@/assets';

const uploadMessage = { 
    image: '지원 형식: jpg, jpeg, png, heic\n최대 30개까지 업로드할 수 있어요.\n업로드 시 전체 파일 용량 합계는 50MB를 넘을 수 없어요.',
    file: '지원 형식: csv, xlsx, pdf\n한 번에 1개의 파일만 업로드 가능해요.\n파일 크기는 20MB을 넘을 수 없어요.'
}

const UploadBox = () => {
  return (
    <div className="bg-background-alternative flex flex-col items-center justify-center gap-5 py-10">
      <Icons.UploadFile className="text-label-neutral h-16 w-16" />
      <h3 className="body2-normal-bold text-label-alternative text-center whitespace-pre-line">
        {'여기에 파일을 드래그하거나\n클릭하여 업로드하세요.'}
      </h3>
      <p className="caption1-medium text-label-alternative text-center whitespace-pre-line leading-relaxed">
        {uploadMessage.image}
      </p>
    </div>
  );
};

export default UploadBox;
