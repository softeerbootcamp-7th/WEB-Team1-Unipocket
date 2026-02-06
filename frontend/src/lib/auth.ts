import { QueryClient, queryOptions } from '@tanstack/react-query';

import { customFetch } from '@/api/config/client';

type User = {
  userId: string;
  email: string;
  nickname: string;
  profileImageUrl: string;
  socialProvider: string;
};

// 쿠키에서 특정 이름의 값을 가져오는 함수
const getCookie = (name: string): string | null => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop()?.split(';').shift() || null;
  }
  return null;
};

// 쿠키의 만료시간을 확인하여 토큰이 유효한지 체크
const isTokenValid = (cookieName: string): boolean => {
  // 쿠키가 존재하는지 확인
  const token = getCookie(cookieName);
  if (!token) {
    return false;
  }

  // 브라우저가 자동으로 만료된 쿠키를 삭제하므로
  // 쿠키가 존재한다면 유효한 것으로 간주
  return true;
};

const isAccessTokenValid = (): boolean => {
  return isTokenValid('access_token');
};

const fetchUser = async (): Promise<User> => {
  const data = await customFetch<User>({ endpoint: '/users/me' });
  console.log(data.nickname);
  return data;
};

const userQueryOptions = queryOptions({
  queryKey: ['auth', 'user'],
  queryFn: fetchUser,
  staleTime: 1000 * 60 * 10, // 유저가 F5를 누르거나, 10분이 지나서 페이지를 이동할 때만 재요청함
  retry: false, // React Query 레벨에서 재시도 방지 (customFetch에서 이미 토큰 재발급 처리함)
  refetchOnWindowFocus: true, // 창을 다시 켰을 때는 체크 -> 다른 탭에서 로그아웃 했을 수도 있으니까
});

const checkAuth = async (queryClient: QueryClient) => {
  const user = await queryClient.ensureQueryData(userQueryOptions);
  return user;
};

export {
  checkAuth,
  fetchUser,
  getCookie,
  isAccessTokenValid,
  userQueryOptions,
};
export type { User };
