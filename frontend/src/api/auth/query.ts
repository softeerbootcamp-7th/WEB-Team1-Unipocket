import { useMutation, useSuspenseQuery } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';

import { logout } from '@/api/auth/api';
import { getUser } from '@/api/user/api';
import { queryClient } from '@/main';

export const useLogoutMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: logout,
    throwOnError: true,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['getUser'] });
      navigate({ to: '/login' });
    },
  });
};

export const useGetUserQuery = () =>
  useSuspenseQuery({
    queryKey: ['getUser'],
    queryFn: getUser,
  });
