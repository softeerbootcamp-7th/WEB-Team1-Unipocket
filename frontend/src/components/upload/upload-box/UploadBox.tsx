import { useRef } from 'react';
import { clsx } from 'clsx';

import { useDragAndDrop } from '@/hooks/useDragAndDrop';

import {
  uploadPolicy,
  useFileValidator,
} from '@/components/upload/upload-box/useFileValidator';

import { Icons } from '@/assets';

interface UploadBoxProps {
  type: 'image' | 'file';
  onFilesSelected: (files: File[]) => void;
  message?: string;
}

const UploadBox = ({ type, onFilesSelected, message }: UploadBoxProps) => {
  const policy = uploadPolicy[type];
  const validateFiles = useFileValidator(policy);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const handleFiles = (fileList: FileList | null) => {
    if (!fileList) return;
    const files = validateFiles(fileList);
    if (!files) return;
    onFilesSelected(files);
  };

  const { isDragging, bind } = useDragAndDrop(handleFiles);

  // 클릭 업로드
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    handleFiles(e.target.files);
    e.target.value = '';
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      fileInputRef.current?.click();
    }
  };

  return (
    <label
      className={clsx(
        'bg-background-normal flex h-full w-full cursor-pointer flex-col items-center justify-center gap-5 rounded-lg border-2 border-dashed py-10 transition-colors',
        isDragging
          ? 'border-blue-400 bg-blue-50'
          : 'hover:bg-background-alternative border-line-normal-strong',
      )}
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
        {message ?? policy.message}
      </p>
    </label>
  );
};

export default UploadBox;
