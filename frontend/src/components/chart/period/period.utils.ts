/**
 * 기간별 차트에서 사용하는 날짜 유틸 함수들
 */

// ─── 월별 (최근 6개월) ───────────────────────────────────

/**
 * 현재 월 포함 과거 6개월의 라벨 생성
 * 예: ['9', '10', '11', '12', '1', '2']
 */
export const generateMonthlyLabels = (today: Date = new Date()): string[] => {
  const labels: string[] = [];
  for (let i = 5; i >= 0; i--) {
    const d = new Date(today.getFullYear(), today.getMonth() - i, 1);
    labels.push(`${d.getMonth() + 1}`);
  }
  return labels;
};

// ─── 주별 (최근 5주) ─────────────────────────────────────

/**
 * 주어진 날짜가 속한 주의 월요일을 반환
 */
const getMonday = (date: Date): Date => {
  const d = new Date(date);
  const day = d.getDay(); // 0=일, 1=월 ... 6=토
  const diff = day === 0 ? -6 : 1 - day; // 일요일이면 -6, 그 외 1-day
  d.setDate(d.getDate() + diff);
  d.setHours(0, 0, 0, 0);
  return d;
};

/**
 * 해당 날짜가 그 월의 몇 번째 주인지 계산 (월요일 기준)
 * 1일이 속한 주 = 1주차
 */
const getWeekOfMonth = (date: Date): number => {
  // 해당 월 1일의 요일 (월=0 ~ 일=6 으로 변환)
  const firstDay = new Date(date.getFullYear(), date.getMonth(), 1);
  const firstDayOfWeek = (firstDay.getDay() + 6) % 7; // 월=0

  return Math.ceil((date.getDate() + firstDayOfWeek) / 7);
};

/**
 * 현재 주 포함 과거 5주의 라벨 생성
 * 주 정의: 월요일 ~ 일요일
 */
export const generateWeeklyLabels = (today: Date = new Date()): string[] => {
  const labels: string[] = [];
  const currentMonday = getMonday(today);

  for (let i = 4; i >= 0; i--) {
    const monday = new Date(currentMonday);
    monday.setDate(monday.getDate() - i * 7);

    const month = monday.getMonth() + 1;
    const weekNum = getWeekOfMonth(monday);

    labels.push(`${month}월 ${weekNum}주`);
  }

  return labels;
};

// ─── 일별 (최근 7일) ─────────────────────────────────────

/**
 * 오늘 포함 과거 7일의 라벨 생성
 * 예: ['2/5', '2/6', '2/7', '2/8', '2/9', '2/10', '2/11']
 */
export const generateDailyLabels = (today: Date = new Date()): string[] => {
  const labels: string[] = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    labels.push(`${d.getMonth() + 1}/${d.getDate()}`);
  }
  return labels;
};
