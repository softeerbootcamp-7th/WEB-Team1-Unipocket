import { queryOptions } from '@tanstack/react-query';
import { redirect } from '@tanstack/react-router';

import { customFetch } from '@/api/config/client';

type User = {
  id: string;
  name: string;
  email: string;
};

// BFF API 호출
export const fetchCurrentUser = async (): Promise<User | null> => {
  // 1. try-catch를 제거합니다.
  // 에러가 나면 그대로 밖(React Query 및 전역 핸들러)으로 던져져야 합니다.

  const data = await customFetch('/auth/me', { method: 'GET' });

  // customFetch가 이미 내부에서 response.json()을 반환하므로 바로 data를 사용합니다.
  return data;
};

export const userQueryOptions = queryOptions({
  queryKey: ['auth', 'user'],
  queryFn: fetchCurrentUser,

  // 유저가 F5를 누르거나, 10분이 지나서 페이지를 이동할 때만 재요청함
  staleTime: 1000 * 60 * 10,

  // 401 에러가 났을 때 재시도하면 무한 루프 돌 수 있으니 끔
  retry: false,

  // 창을 다시 켰을 때(refetchOnWindowFocus)는 체크하는 게 좋음
  // (사용자가 다른 탭에서 로그아웃 했을 수도 있으니까)
  refetchOnWindowFocus: true,
});

// TODO: 개발용 임시 모킹 함수 (백엔드 연동 시 제거 또는 ensureQueryData로 대체)
export const mockCheckAuth = async () => {
  // API 대신 사용할 임시 유저 데이터
  const mockUser = {
    id: '1',
    name: '홍길동',
    email: 'honggildong@example.com',
  };

  // 실제 API 호출 느낌을 내기 위한 0.5초 대기 로직
  return new Promise<typeof mockUser | null>((resolve) => {
    setTimeout(() => {
      resolve(mockUser); // 0.5초 뒤에 홍길동 데이터를 반환
      // resolve(null); // 만약 로그인 안 된 상태를 테스트하고 싶다면 null을 넣으세요.
    }, 500);
  });
};

export const requireAuth = async (locationHref: string) => {
  const user = await mockCheckAuth();
  if (!user) {
    throw redirect({
      to: '/login',
      search: { redirect: locationHref },
    });
  }
  return user;
};

export const redirectIfAuthenticated = async () => {
  const user = await mockCheckAuth();
  if (user) {
    throw redirect({
      to: '/home',
    });
  }
  return user;
};
