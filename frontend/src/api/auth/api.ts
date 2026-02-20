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

// ==========================================
// 3. Non-API Functions (Route Guards / Utils)
// ==========================================
export const redirectIfAuthenticated = async () => {
  try {
    const user = await getUser();
    if (user) {
      throw redirect({
        to: '/home',
      });
    }
  } catch (error) {
    if (isRedirect(error)) {
      throw error;
    }
  }
};

export const redirectIfNeedsOnboarding = async () => {
  try {
    const user = await getUser();
    if (user && user.needsOnboarding) {
      throw redirect({
        to: '/init',
      });
    }
  } catch (error) {
    if (isRedirect(error)) {
      throw error;
    }
  }
};

export const redirectIfBannedOrDeleted = async () => {
  try {
    const user = await getUser();
    if (
      user &&
      (user.status === 'BANNED' ||
        user.status === 'DELETED' ||
        user.status === 'INACTIVE')
    ) {
      throw redirect({
        to: '/',
      });
    }
  } catch (error) {
    if (isRedirect(error)) {
      throw error;
    }
  }
};
