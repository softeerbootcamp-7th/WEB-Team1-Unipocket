export const dayNames = ['일', '월', '화', '수', '목', '금', '토'];

// 두 Date 객체가 같은 날짜인지 확인
export const isSameDay = (a: Date | null, b: Date | null) => {
  if (!a || !b) return false;

  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
};

// 달력을 렌더링하기 위한 배열 생성 (항상 6주 = 42일)
export const getCalendarDateArr = (date: Date) => {
  const result: { day: number; isCurrentMonth: boolean; date: Date }[] = [];
  const year = date.getFullYear();
  const month = date.getMonth();

  const currentMonthFirstDate = new Date(year, month, 1);
  const firstDayOfWeek = currentMonthFirstDate.getDay(); // 0(일) ~ 6(토)

  // 1일이 일요일(0)이면 2줄부터 시작하기 위해 이전 달 1주(7일) 추가 아니면 요일만큼만 추가
  const prevDaysCount = firstDayOfWeek === 0 ? 7 : firstDayOfWeek;

  // 42일치 날짜 생성 (이전 달 끝부분 + 현재 달 + 다음 달 초반)
  for (let i = 0; i < 42; i++) {
    // month와 day 파라미터가 범위를 벗어나면 Date 객체가 자동으로 월/년을 조정합니다.
    const currentDate = new Date(year, month, 1 - prevDaysCount + i);

    result.push({
      day: currentDate.getDate(),
      isCurrentMonth: currentDate.getMonth() === month,
      date: currentDate,
    });
  }

  return result;
};

// 날짜를 'YYYY.MM.DD. (요일) HH:MM' 형식으로 포맷
export const formatDateTime = (date: Date): string => {
  const formatter = new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'short',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false // 24시간 형식
  });

  const parts = formatter.formatToParts(date);
  const year = parts.find(p => p.type === 'year')?.value;
  const month = parts.find(p => p.type === 'month')?.value;
  const day = parts.find(p => p.type === 'day')?.value;
  const weekday = parts.find(p => p.type === 'weekday')?.value;
  const hour = parts.find(p => p.type === 'hour')?.value;
  const minute = parts.find(p => p.type === 'minute')?.value;

  return `${year}.${month}.${day}. (${weekday}) ${hour}:${minute}`;
};
