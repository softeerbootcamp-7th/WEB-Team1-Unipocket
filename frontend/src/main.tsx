import './styles/index.css';

import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { AuthProvider } from 'react-oidc-context';
import { QueryClient } from '@tanstack/react-query';
import { createRouter, RouterProvider } from '@tanstack/react-router';

import { routeTree } from './routeTree.gen';

const queryClient = new QueryClient();

const router = createRouter({
  routeTree,
  context: {
    // 여기서 MyRouterContext 형식에 맞는 객체를 넘겨줘야 합니다.
    // 보통 queryClient나 auth 정보를 넘깁니다.
    auth: undefined!, // 일단 타입을 맞추기 위해 이렇게 두거나, 초기값을 넣습니다.
    queryClient: queryClient,
  },
});

const oidcConfig = {
  authority: '<your authority>',
  client_id: '<your client id>',
  redirect_uri: '<your redirect uri>',
  // ...
};

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider {...oidcConfig}>
      <RouterProvider router={router} />
    </AuthProvider>
  </StrictMode>,
);
