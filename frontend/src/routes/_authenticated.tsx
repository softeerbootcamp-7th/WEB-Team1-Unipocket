import { createFileRoute, isRedirect, redirect } from '@tanstack/react-router';

import useAuth from '@/store/auth';

// src/routes/_authenticated.tsx
export const Route = createFileRoute('/_authenticated')({
  beforeLoad: async ({ location }) => {
    try {
      // 1. API 호출로 실제 세션 검증
      const user = await verifySession();

      // 2. 검증 실패 시
      if (!user) {
        throw redirect({
          to: '/login',
          search: { redirect: location.href },
        });
      }

      // 3. 검증 성공 시 Zustand에서 최초 방문 여부 상태 동기화
      useAuth.getState().setHasVisited(true);

      // 4. 리턴된 값은 이 라우트의 모든 하위 페이지에서 context로 접근 가능
      return { user };
    } catch (error) {
      if (isRedirect(error)) throw error;

      // API 에러(401 등) 시 로그인으로 리다이렉트
      throw redirect({
        to: '/login',
        search: { redirect: location.href },
      });
    }
  },
});

function verifySession() {
  return Promise.resolve({ id: 1, name: 'John Doe' });
  //   throw new Error('Function not implemented.');
}
