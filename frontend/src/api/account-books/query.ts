import { queryOptions, useSuspenseQuery } from '@tanstack/react-query';

import { getAccountBooks } from './api';

export const accountBooksQueryOptions = queryOptions({
  queryKey: ['accountBooks'],
  queryFn: getAccountBooks,
});

export const useGetAccountBooksQuery = () => {
  return useSuspenseQuery(accountBooksQueryOptions);
};
