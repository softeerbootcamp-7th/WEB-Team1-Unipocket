import { useCallback } from 'react';

interface UploadPolicy {
  message: string;
  accept: string;
  multiple: boolean;
  maxCount: number;
  maxTotalSize: number;
}

export const uploadPolicy = {
  image: {
    message:
      '지원 형식: jpg, jpeg, png\n최대 30개까지 업로드할 수 있어요.\n업로드 시 전체 파일 용량 합계는 50MB를 넘을 수 없어요.',
    accept: 'image/jpeg,image/png,image/jpg',
    multiple: true,
    maxCount: 30,
    maxTotalSize: 50 * 1024 * 1024,
  },
  landingImage: {
    accept: 'image/jpeg,image/png,image/jpg',
    multiple: true,
    maxCount: 3,
    maxTotalSize: 20 * 1024 * 1024,
  },
  file: {
    message:
      '지원 형식: csv, xlsx\n한 번에 1개의 파일만 업로드 가능해요.\n파일 크기는 20MB을 넘을 수 없어요.',
    accept: '.csv,.xlsx',
    multiple: false,
    maxCount: 1,
    maxTotalSize: 20 * 1024 * 1024,
  },
} as const;

export const useFileValidator = (policy: UploadPolicy) => {
  return useCallback(
    (fileList: FileList | null) => {
      if (!fileList) return null;

      const files = Array.from(fileList);

      // 1. 개수 제한
      if (!policy.multiple && files.length > 1) {
        alert('한 번에 하나의 파일만 업로드할 수 있어요.');
        return null;
      }

      if (files.length > policy.maxCount) {
        alert(`최대 ${policy.maxCount}개까지 업로드할 수 있어요.`);
        return null;
      }

      // 2. 파일 형식
      const allowed = policy.accept
        .split(',')
        .map((v) => v.trim().toLowerCase());

      const hasInvalid = files.some((file) => {
        const name = file.name.toLowerCase();
        const type = file.type.toLowerCase();

        return !allowed.some((a) =>
          a.startsWith('.') ? name.endsWith(a) : type === a,
        );
      });

      if (hasInvalid) {
        alert('지원하지 않는 파일 형식이 포함되어 있어요.');
        return null;
      }

      // 3. 파일 용량
      const totalSize = files.reduce((sum, file) => sum + file.size, 0);
      if (totalSize > policy.maxTotalSize) {
        const maxMB = policy.maxTotalSize / (1024 * 1024);
        alert(`전체 파일 용량은 ${maxMB}MB를 초과할 수 없어요.`);
        return null;
      }

      return files;
    },
    [policy],
  );
};
