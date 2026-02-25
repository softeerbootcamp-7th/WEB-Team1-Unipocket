import { useMutation } from '@tanstack/react-query';

import { logout } from '@/api/auth/api';
import { queryClient } from '@/main';

export const useLogoutMutation = () => {
  return useMutation({
    mutationFn: logout,
    onSettled: () => {
      queryClient.clear(); // 모든 쿼리 제거
      window.location.href = '/'; // 페이지 강제 리로드로 깔끔하게 초기화
    },
  });
};
