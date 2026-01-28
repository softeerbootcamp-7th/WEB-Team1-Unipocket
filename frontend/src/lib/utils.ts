import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// 현재 로컬 시간을 '오전 09:05' 형식으로 반환하는 유틸 함수
export const getLocalTime = () => {
  return new Intl.DateTimeFormat('ko-KR', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
    timeZone: 'Asia/Seoul', // 현재는 한국 시간만을 기준으로 사용
  }).format(new Date());
};
