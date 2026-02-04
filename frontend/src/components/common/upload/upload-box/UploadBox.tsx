import { useCallback, useRef } from 'react';

import { useDragAndDrop } from '@/hooks/useDragAndDrop';

import { Icons } from '@/assets';

import { uploadPolicy, useFileValidator } from './useFileValidator';

interface UploadBoxProps {
  type: 'image' | 'file';
}

const UploadBox = ({ type }: UploadBoxProps) => {
  const policy = uploadPolicy[type];
  const validateFiles = useFileValidator(policy);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const {
    isDragging,
    bind,
  } = useDragAndDrop(validateFiles);

  // 클릭 업로드
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = validateFiles(e.target.files);
    if (!files) return;

    console.log('업로드 준비 완료:', files);
    e.target.value = '';
  };

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      fileInputRef.current?.click();
    }
  }, []);

  return (
    <label
      className={`bg-background-normal flex cursor-pointer flex-col items-center justify-center gap-5 border-2 border-dashed py-10 transition-colors ${
        isDragging
          ? 'border-blue-400 bg-blue-50'
          : 'hover:bg-background-alternative border-transparent'
      } `}
      {...bind}
      tabIndex={0}
      onKeyDown={handleKeyDown}
    >
      <input
        ref={fileInputRef}
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
      <p className="caption1-medium text-label-alternative text-center whitespace-pre-line">
        {policy.message}
      </p>
    </label>
  );
};

export default UploadBox;
