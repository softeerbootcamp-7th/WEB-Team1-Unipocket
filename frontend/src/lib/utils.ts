import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

import { dayNames } from '@/components/common/calendar/date.utils';

import { COUNTRY_TIME_REGION, TIME_REGION_CONFIG } from '@/constants/time';
import type { CountryCode } from '@/data/countryCode';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// 현재 로컬 시간을 '오전 09:05' 형식으로 반환하는 유틸 함수
export const getLocalTime = (country: CountryCode) => {
  const region = COUNTRY_TIME_REGION[country] ?? 'DEFAULT';

  const { locale, timeZone, hour12 } = TIME_REGION_CONFIG[region];

  return new Intl.DateTimeFormat(locale, {
    hour: 'numeric',
    minute: '2-digit',
    hour12,
    timeZone,
  }).format(new Date());
};

/**
 * Date 객체를 'YYYY-MM-DD' 형식의 문자열로 변환
 * @param date 변환할 Date 객체
 * @returns 'YYYY-MM-DD' 형식의 문자열
 */
export const formatDateToString = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
};

/**
 * 'YYYY-MM-DD' 형식의 문자열을 Date 객체로 변환
 * @param dateString 'YYYY-MM-DD' 형식의 문자열
 * @returns Date 객체
 */
export const parseStringToDate = (dateString: string): Date => {
  const [year, month, day] = dateString.split('-').map(Number);
  return new Date(year, month - 1, day);
};

/**
 * Date 객체를 'YYYY.M.D.요일' 형식의 문자열로 변환
 * @param date 변환할 Date 객체
 * @returns 'YYYY.M.D.요일' 형식의 문자열
 */
export const formatDateWithDay = (date: Date): string => {
  const weekDay = dayNames[date.getDay()];
  return `${date.getFullYear()}.${date.getMonth() + 1}.${date.getDate()}.${weekDay}`;
};
