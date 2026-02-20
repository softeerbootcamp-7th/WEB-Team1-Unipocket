const EMPTY_TEXT = '비어 있음';

export function EmptyValue() {
  return <span className="text-label-assistive">{EMPTY_TEXT}</span>;
}

export { EMPTY_TEXT };
