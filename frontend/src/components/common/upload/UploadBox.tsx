import { Icons } from '@/assets';
interface UploadBoxProps {
  type: 'image' | 'file';
}

const uploadPolicy = {
  image: {
    message:
      '지원 형식: jpg, jpeg, png\n최대 30개까지 업로드할 수 있어요.\n업로드 시 전체 파일 용량 합계는 50MB를 넘을 수 없어요.',
    accept: 'image/jpeg,image/png,image/jpg',
    multiple: true,
    maxCount: 30,
    maxTotalSize: 50 * 1024 * 1024, // 50MB
  },
  file: {
    message:
      '지원 형식: csv, xlsx\n한 번에 1개의 파일만 업로드 가능해요.\n파일 크기는 20MB을 넘을 수 없어요.',
    accept: '.csv,.xlsx',
    multiple: false,
    maxCount: 1,
    maxTotalSize: 20 * 1024 * 1024, // 20MB
  },
} as const;

const UploadBox = ({ type }: UploadBoxProps) => {
  const policy = uploadPolicy[type];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { files: fileList } = e.target;
    if (!fileList || fileList.length === 0) return;

    const files = Array.from(fileList);

    // 1. 개수 제한 (multiple: false인 경우도 포함)
    if (!policy.multiple && files.length > 1) {
      alert('한 번에 하나의 파일만 업로드할 수 있습니다.');
      e.target.value = '';
      return;
    }

    if (files.length > policy.maxCount) {
      alert(`최대 ${policy.maxCount}개까지 업로드할 수 있어요.`);
      e.target.value = '';
      return;
    }

    // 2. 파일 형식 검증 (isValidType 로직 개선)
    const allowedExtensions = policy.accept
      .split(',')
      .map((t) => t.trim().toLowerCase());

    const hasInvalidFile = files.some((file) => {
      const fileName = file.name.toLowerCase();
      const fileType = file.type.toLowerCase();

      return !allowedExtensions.some((allowed) => {
        if (allowed.startsWith('.')) {
          return fileName.endsWith(allowed); // 확장자 체크 (.csv)
        }
        return fileType === allowed; // MIME 타입 체크 (image/jpeg)
      });
    });

    if (hasInvalidFile) {
      alert('지원하지 않는 파일 형식이 포함되어 있어요.');
      e.target.value = '';
      return;
    }

    // 3. 개별 파일 용량 & 전체 용량 제한
    const totalSize = files.reduce((sum, file) => sum + file.size, 0);
    if (totalSize > policy.maxTotalSize) {
      const maxSizeMB = policy.maxTotalSize / (1024 * 1024);
      alert(`전체 파일 용량은 ${maxSizeMB}MB를 초과할 수 없습니다.`);
      e.target.value = '';
      return;
    }

    // @TODO: 성공 시 로직 추가
    console.log('업로드 준비 완료:', files);
    // onUpload(files); // Props로 받은 업로드 함수 실행 등
  };

  return (
    <label className="hover:bg-background-alternative flex cursor-pointer flex-col items-center justify-center gap-5 py-10">
      <input
        type="file"
        className="hidden"
        accept={policy.accept}
        multiple={policy.multiple}
        onChange={handleChange}
      />

      <Icons.UploadFile className="text-label-neutral h-16 w-16" />
      <h3 className="body2-normal-bold text-label-alternative text-center whitespace-pre-line">
        {'여기에 파일을 드래그하거나\n클릭하여 업로드하세요.'}
      </h3>
      <p className="caption1-medium text-label-alternative text-center leading-relaxed whitespace-pre-line">
        {policy.message}
      </p>
    </label>
  );
};

export default UploadBox;
