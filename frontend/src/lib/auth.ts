import { QueryClient, queryOptions } from '@tanstack/react-query';

import { customFetch } from '@/api/config/client';

type User = {
  userId: string;
  email: string;
  nickname: string;
  profileImageUrl: string;
  socialProvider: string;
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

export { checkAuth, fetchUser, userQueryOptions };
export type { User };
