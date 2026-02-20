import { useMutation, useSuspenseQuery } from '@tanstack/react-query';

import { logout } from '@/api/auth/api';
import { getUser } from '@/api/users/api';
import { userQueryOptions } from '@/api/users/query';
import { queryClient } from '@/main';

// --- Queries ---
export const useGetUserQuery = () => {
  return useSuspenseQuery({
    queryKey: userQueryOptions.queryKey,
    queryFn: getUser,
  });
};

// --- Mutations ---
export const useLogoutMutation = () => {
  return useMutation({
    mutationFn: logout,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: userQueryOptions.queryKey });
    },
  });
};
