import { clsx } from 'clsx';

export interface YearMonth {
  year: number;
  month: number;
}

/**
 * 날짜 문자열(YYYY-MM)을 년월 객체로 파싱
 */
export const parseYearMonth = (
  dateString: string | undefined,
): YearMonth | null => {
  if (!dateString) return null;
  const [year, month] = dateString.split('-').map(Number);
  return { year, month };
};

/**
 * Date 객체를 년월 객체로 변환
 */
export const dateToYearMonth = (date: Date): YearMonth => ({
  year: date.getFullYear(),
  month: date.getMonth() + 1,
});

/**
 * endDate와 오늘 중 이른 날짜를 선택
 */
export const getMaxYearMonth = (
  endYearMonth: YearMonth | null,
  nowYearMonth: YearMonth,
): YearMonth => {
  if (!endYearMonth) return nowYearMonth;

  if (
    endYearMonth.year < nowYearMonth.year ||
    (endYearMonth.year === nowYearMonth.year &&
      endYearMonth.month < nowYearMonth.month)
  ) {
    return endYearMonth;
  }
  return nowYearMonth;
};

/**
 * 월 이동 가능 여부 체크
 */
export const canGoToMonth = (
  selectedDate: Date,
  offset: number,
  startYearMonth: YearMonth | null,
  maxYearMonth: YearMonth,
): boolean => {
  const targetDate = new Date(selectedDate);
  targetDate.setMonth(targetDate.getMonth() + offset);
  const targetYear = targetDate.getFullYear();
  const targetMonth = targetDate.getMonth() + 1;

  if (offset < 0) {
    if (!startYearMonth) return true;
    return (
      targetYear > startYearMonth.year ||
      (targetYear === startYearMonth.year &&
        targetMonth >= startYearMonth.month)
    );
  } else {
    return (
      targetYear < maxYearMonth.year ||
      (targetYear === maxYearMonth.year && targetMonth <= maxYearMonth.month)
    );
  }
};

/**
 * 네비게이션 버튼 클래스 생성
 */
export const getNavButtonClass = (enabled: boolean) =>
  clsx('size-6', {
    'text-label-alternative cursor-pointer': enabled,
    'text-label-disable': !enabled,
  });
