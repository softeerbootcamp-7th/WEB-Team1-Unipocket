import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

import { DAY_NAMES } from '@/components/calendar/date.utils';

import { COUNTRY_TIME_REGION, TIME_REGION_CONFIG } from '@/constants/time';
import type { CountryCode } from '@/data/country/countryCode';

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
 */
export const formatDateToString = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
};

/**
 * 'YYYY-MM-DD' 형식의 문자열을 Date 객체로 변환
 */
export const parseStringToDate = (dateString: string): Date => {
  const [year, month, day] = dateString.split('-').map(Number);
  return new Date(year, month - 1, day);
};

/**
 * Date 객체를 'YYYY.M.D.요일' 형식의 문자열로 변환
 */
export const formatDateWithDay = (date: Date): string => {
  const weekDay = DAY_NAMES[date.getDay()];
  return `${date.getFullYear()}.${date.getMonth() + 1}.${date.getDate()}.${weekDay}`;
};

/**
 * Date 객체를 ISO 8601 UTC 형식(예: 2026-02-07T15:00:00.000Z)으로 변환합니다.
 * @param date 변환할 날짜
 * @param isEndOfDay true면 해당 일의 끝(23:59:59), false면 시작(00:00:00)으로 설정
 */
export const formatToISODateTime = (
  date: Date,
  isEndOfDay: boolean = false,
): string => {
  // 원본 date 객체가 변하지 않도록 복사본 생성
  const targetDate = new Date(date);

  if (isEndOfDay) {
    targetDate.setHours(23, 59, 59, 999);
  } else {
    targetDate.setHours(0, 0, 0, 0);
  }

  return targetDate.toISOString();
};
