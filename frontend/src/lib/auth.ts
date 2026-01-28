import { queryOptions } from '@tanstack/react-query';

import { customFetch } from '@/apis/client';

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
