import { clsx } from 'clsx';

import Button from '@/components/common/Button';

import { Icons } from '@/assets';

type SnackbarStatus = 'loading' | 'success' | 'default';

interface SnackbarProps {
  status?: SnackbarStatus;
  title?: string;
  description?: string;
  btnText?: string;
  onAction: () => void;
}

const Snackbar = ({
  status = 'default',
  title,
  description,
  btnText = '결과 확인',
  onAction,
}: SnackbarProps) => {
  const iconMap = {
    loading: (
      <Icons.Loading className="text-inverse-label size-5.5 animate-spin" />
    ),
    success: <Icons.CheckmarkCircle className="size-5.5" />,
    default: null,
  };

  const titleMap: Record<SnackbarStatus, string> = {
    loading: '지출 내역 분석 중...',
    success: '지출 내역 분석 완료',
    default: '',
  };

  const isLoading = status === 'loading';
  const isSuccess = status === 'success';
  return (
    <div
      role="status"
      className="z-priority rounded-modal-12 fixed bottom-10 left-10 flex w-83.75 max-w-105 items-center justify-between gap-8 bg-[#49494B] px-4 py-2.75"
    >
      <div className="flex items-center gap-2">
        {iconMap[status]}
        <div className="text-inverse-label flex flex-col items-start justify-center px-0.5">
          <p className="body2-normal-bold">{title ?? titleMap[status]}</p>
          {description && <p className="label2-regular">{description}</p>}
        </div>
      </div>
      <Button
        variant="outlined-inverse"
        className={clsx({
          'shrink-0': true,
          'border-line-solid-strong': isSuccess,
        })}
        onClick={onAction}
        disabled={isLoading}
      >
        {btnText}
      </Button>
    </div>
  );
};

export default Snackbar;
