import { useMutation } from '@tanstack/react-query';

import { logout } from '@/api/auth/api';
import { userQueryOptions } from '@/api/users/query';
import { queryClient } from '@/main';

export const useLogoutMutation = () => {
  return useMutation({
    mutationFn: logout,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: userQueryOptions.queryKey });
    },
  });
};
