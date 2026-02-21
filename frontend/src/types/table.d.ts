import '@tanstack/react-table';

declare module '@tanstack/react-table' {
  interface ColumnMeta<TData, TValue> {
    _data?: TData; 
    _value?: TValue;
    cellEditor?: 'text' | 'category' | 'amount' | 'method';
  }
}