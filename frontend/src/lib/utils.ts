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
 * Date 객체를 백엔드가 요구하는 OffsetDateTime 형식(예: 2026-02-08T00:00:00+09:00)으로 변환
 * @param date 변환할 날짜
 * @param isEndOfDay true면 23:59:59, false면 00:00:00으로 설정
 */
export const toOffsetDateTimeString = (
  date: Date,
  isEndOfDay: boolean = false,
): string => {
  const targetDate = new Date(date);

  // 시작일은 00:00:00, 종료일은 23:59:59로 시간 세팅
  if (isEndOfDay) {
    targetDate.setHours(23, 59, 59, 999);
  } else {
    targetDate.setHours(0, 0, 0, 0);
  }

  // 타임존 오프셋 계산 (한국은 +09:00)
  const offset = targetDate.getTimezoneOffset();
  const sign = offset > 0 ? '-' : '+';
  const absOffset = Math.abs(offset);
  const hours = String(Math.floor(absOffset / 60)).padStart(2, '0');
  const minutes = String(absOffset % 60).padStart(2, '0');

  // 로컬 시간 기준으로 YYYY-MM-DDTHH:mm:ss 문자열 조립
  const year = targetDate.getFullYear();
  const month = String(targetDate.getMonth() + 1).padStart(2, '0');
  const day = String(targetDate.getDate()).padStart(2, '0');
  const hour = String(targetDate.getHours()).padStart(2, '0');
  const minute = String(targetDate.getMinutes()).padStart(2, '0');
  const second = String(targetDate.getSeconds()).padStart(2, '0');

  // 최종 OffsetDateTime 규격 완성
  return `${year}-${month}-${day}T${hour}:${minute}:${second}${sign}${hours}:${minutes}`;
};
