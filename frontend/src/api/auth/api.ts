import { isRedirect, redirect } from '@tanstack/react-router';

import type { LoginResponse } from '@/api/auth/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import { getUser } from '@/api/users/api';

export const logout = () => {
  return customFetch({
    endpoint: ENDPOINTS.AUTH.LOGOUT,
    options: {
      method: 'POST',
    },
  });
};

export const loginDev = (): Promise<LoginResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.AUTH.LOGIN_DEV,
    params: { userId: import.meta.env.VITE_USER_ID },
    options: {
      method: 'POST',
    },
  });
};

export const requireGuest = async () => {
  try {
    const user = await getUser().catch(() => null); // 에러 발생 시 비회원으로 간주

    if (user) {
      if (
        user.status === 'BANNED' ||
        user.status === 'DELETED' ||
        user.status === 'INACTIVE'
      ) {
        return; // 정지/탈퇴 유저는 _auth(랜딩)에 머물게 둠
      }

      // 온보딩(가계부 생성) 필요 여부에 따라 리다이렉트
      throw redirect({
        to: user.needsOnboarding ? '/init' : '/home',
      });
    }
  } catch (error) {
    if (isRedirect(error)) throw error;
  }
};

/**
 * [_app 전용] 로그인되지 않은 유저나 정지된 유저의 접근을 막습니다.
 */
export const requireAuth = async () => {
  try {
    const user = await getUser().catch(() => null);

    // 1. 비회원이거나 정지된 계정은 랜딩('/')으로 쫓아냄
    if (
      !user ||
      user.status === 'BANNED' ||
      user.status === 'DELETED' ||
      user.status === 'INACTIVE'
    ) {
      throw redirect({ to: '/' });
    }

    return user; // 통과한 유저 정보를 반환하여 _app에서 활용할 수 있게 함
  } catch (error) {
    if (isRedirect(error)) throw error;
  }
};
