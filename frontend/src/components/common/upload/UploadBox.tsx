import { useCallback, useRef, useState } from 'react';

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
  const fileIInputRef = useRef<HTMLInputElement | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  const validateAndProcessFiles = useCallback((fileList: FileList | null) => {
    if (!fileList) return;
    const files = Array.from(fileList);

    // 1. 개수 제한 (multiple: false인 경우도 포함)
    if (!policy.multiple && files.length > 1) {
      alert('한 번에 하나의 파일만 업로드할 수 있어요.');
      return;
    }

    if (files.length > policy.maxCount) {
      alert(`최대 ${policy.maxCount}개까지 업로드할 수 있어요.`);
      return;
    }

    // 2. 파일 형식 검증 (isValidType 로직 개선)
    const allowedExtensions = policy.accept
      .split(',')
      .map((t) => t.trim().toLowerCase());

    const hasInvalidFile = files.some((file) => {
      const fileName = file.name.toLowerCase();
      const fileType = file.type.toLowerCase();

      return !allowedExtensions.some(allowed => 
        allowed.startsWith('.') ? fileName.endsWith(allowed) : fileType === allowed
      );
    });

    if (hasInvalidFile) {
      alert('지원하지 않는 파일 형식이 포함되어 있어요.');
      return;
    }

    // 3. 개별 파일 용량 & 전체 용량 제한
    const totalSize = files.reduce((sum, file) => sum + file.size, 0);
    if (totalSize > policy.maxTotalSize) {
      const maxSizeMB = policy.maxTotalSize / (1024 * 1024);
      alert(`전체 파일 용량은 ${maxSizeMB}MB를 초과할 수 없어요.`);
      return;
    }

    // @TODO: 성공 시 로직 추가
    console.log('업로드 준비 완료:', files);
    // onUpload(files
  }, [policy]);

  // 드래그 영역에 들어왔을 때
  const onDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  }, []);

  // 드래그 영역에서 벗어났을 때
  const onDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  }, []);

  // 드롭했을 때
  const onDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    const { files } = e.dataTransfer;
    validateAndProcessFiles(files);
  }, [validateAndProcessFiles]);

  // 클릭으로 업로드할 때
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    validateAndProcessFiles(e.target.files);
    e.target.value = ''; // 같은 이름의 파일 재업로드 가능하도록 초기화
  };

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault(); // Space 사용 시 페이지 스크롤 방지
      fileIInputRef.current?.click();
    }
  }, []);

  return (
    <label
      className={`
        flex cursor-pointer flex-col items-center justify-center gap-5 py-10 border-2 border-dashed transition-colors
        ${isDragging ? 'bg-blue-50 border-blue-400' : 'hover:bg-background-alternative border-transparent'}
      `}
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
      onDrop={onDrop}
      tabIndex={0}
      onKeyDown={handleKeyDown}
    >
      <input
        ref={fileIInputRef}
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
