import { useMutation } from '@tanstack/react-query';

import { logout } from '@/api/auth/api';
import { queryClient } from '@/main';
import { useAccountBookStore } from '@/stores/accountBookStore';

export const useLogoutMutation = () => {
  const { clearAccountBook } = useAccountBookStore.getState();
  return useMutation({
    mutationFn: logout,
    throwOnError: false, // 에러 바운더리로 전파하지 않음
    onSettled: () => {
      // 성공/실패 여부와 관계없이 실행 (401 에러 포함)
      queryClient.clear(); // 모든 쿼리 제거
      clearAccountBook();
      window.location.href = '/'; // 페이지 강제 리로드로 깔끔하게 초기화
    },
  });
};
