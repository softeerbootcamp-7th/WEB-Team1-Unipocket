import { type ReactNode, useEffect } from 'react';

import Icon from '@/components/common/Icon';

import Button from '../common/Button';

interface UploadResultModalProps {
  isOpen: boolean;
  imageCount?: number;
  expenseCount: number;
  onClose: () => void;
  onConfirm?: () => void;
  children?: ReactNode;
}

export default function UploadResultModal({
  isOpen,
  imageCount,
  expenseCount,
  onClose,
  onConfirm,
  children,
}: UploadResultModalProps) {
  useEffect(() => {
    if (!isOpen) return;

    const original = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    return () => {
      document.body.style.overflow = original;
    };
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const title = imageCount
    ? `${imageCount}개의 사진에서 ${expenseCount}건의 지출 내역이 생성됐어요.`
    : `${expenseCount}건의 지출 내역이 생성됐어요.`;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="upload-result-title"
      className="bg-background-alternative fixed inset-0 z-50 flex flex-col gap-8"
    >
      <div className="border-line-normal-normal flex h-11.5 items-center justify-between border-b px-8">
        <span className="body2-normal-bold text-label-normal">결과 확인</span>
        <Icon
          iconName="Close"
          color="text-label-neutral"
          width={20}
          height={20}
          ariaLabel="모달 닫기"
          onClick={onClose}
        />
      </div>
      <div className="flex min-h-0 flex-1 flex-col gap-10 px-8">
        <div className="flex flex-col gap-2">
          <h2
            id="upload-result-title"
            className="title2-semibold text-label-normal"
          >
            {title}
          </h2>
          <span className="body1-normal-medium text-label-alternative">
            수입 내역은 제외되어 생성됐어요. 일치하지 않은 정보가 있다면
            수정해주세요.
          </span>
        </div>
        <main className="min-h-0 flex-1 overflow-y-auto">{children}</main>
      </div>
      <footer className="bg-background-normal border-line-normal-neutral flex h-28 justify-end gap-4 border-t p-8">
        <Button variant="outlined" size="lg" onClick={onClose}>
          취소
        </Button>
        <Button variant="solid" size="lg" onClick={onConfirm}>
          지출 내역 적용하기
        </Button>
      </footer>
    </div>
  );
}
