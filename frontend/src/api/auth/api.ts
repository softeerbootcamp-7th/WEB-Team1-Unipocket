import { isRedirect, redirect } from '@tanstack/react-router';

import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import { getUser } from '@/api/user/api';

export const logout = () => {
  return customFetch({
    endpoint: ENDPOINTS.LOGOUT,
    options: {
      method: 'POST',
    },
  });
};

export const redirectIfAuthenticated = async () => {
  try {
    // useQuery 사용하지 않는 이유는 캐싱된 데이터를 사용하지 않고
    // 항상 최신 인증 상태를 확인하기 위함
    const user = await getUser();
    if (user) {
      throw redirect({
        to: '/home',
      });
    }
  } catch (error) {
    // redirect 에러는 다시 throw하여 라우터가 처리하도록 함
    if (isRedirect(error)) {
      throw error;
    }
    // 인증되지 않은 상태(401 등)이면 아무 작업도 하지 않음
  }
};

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  expiresIn: number;
  tokenType: string;
}

export const loginDev = (): Promise<LoginResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.LOGIN_DEV,
    params: { userId: import.meta.env.VITE_USER_ID },
    options: {
      method: 'POST',
    },
  });
};
