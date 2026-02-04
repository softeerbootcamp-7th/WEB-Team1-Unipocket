import { useCallback, useRef } from 'react';

import { useDragAndDrop } from '@/hooks/useDragAndDrop';

import { Icons } from '@/assets';

import { uploadPolicy, useFileValidator } from './useFileValidator';

const LandingUploadBox = () => {
  const policy = uploadPolicy.landingImage;
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
      className={`bg-background-normal py-auto flex h-55 cursor-pointer items-center justify-center gap-4 border-2 border-dashed transition-colors ${
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

      <Icons.UploadFile className="text-label-neutral h-12 w-12" />
      <div className="flex flex-col gap-1.5">
        <h2 className="heading2-bold text-label-alternative">
          클릭하거나 드래그해서 이미지를 업로드해주세요
        </h2>
        <p className="headline1-medium text-label-alternative">
          jpg, jpeg, png (최대 3개, 총 20MB 이하)
        </p>
      </div>
    </label>
  );
};

export default LandingUploadBox;
