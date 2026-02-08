import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

export const logout = () => {
  return customFetch({
    endpoint: ENDPOINTS.LOGOUT,
    options: {
      method: 'POST',
    },
  });
};
