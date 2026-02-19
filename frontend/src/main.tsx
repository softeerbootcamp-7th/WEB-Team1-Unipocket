import '@/styles/index.css';

import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import {
  QueryCache,
  QueryClient,
  QueryClientProvider,
} from '@tanstack/react-query';
import { createRouter, RouterProvider } from '@tanstack/react-router';
import { toast } from 'sonner';

import { TooltipProvider } from '@/components/ui/tooltip';

import { routeTree } from '@/routeTree.gen';

export const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (_, query) => {
      const errorMessage = query.meta?.errorMessage as string;
      toast.error(errorMessage || '데이터를 불러오는데 실패했습니다.');
    },
  }),
  defaultOptions: {
    queries: {
      retry: false,
    },
  },
});

const router = createRouter({
  routeTree,
  context: {
    queryClient,
  },
});

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <TooltipProvider delayDuration={300}>
        <RouterProvider router={router} />
      </TooltipProvider>
    </QueryClientProvider>
  </StrictMode>,
);
