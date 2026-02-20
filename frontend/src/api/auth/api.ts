import { isRedirect, redirect } from '@tanstack/react-router';

import type { LoginResponse } from '@/api/auth/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import { getUser } from '@/api/users/api';
import type { UserStatus } from '@/api/users/type';

const INACTIVE_USER_STATUSES: UserStatus[] = ['BANNED', 'DELETED', 'INACTIVE'];

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
      if (!user || INACTIVE_USER_STATUSES.includes(user.status)) {
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

export const requireAuth = async () => {
  try {
    const user = await getUser().catch(() => null);

    // 1. 비회원이거나 정지된 계정은 랜딩('/')으로 쫓아냄
    if (!user || INACTIVE_USER_STATUSES.includes(user.status)) {
      throw redirect({ to: '/' });
    }

    return user;
  } catch (error) {
    if (isRedirect(error)) throw error;
  }
};
