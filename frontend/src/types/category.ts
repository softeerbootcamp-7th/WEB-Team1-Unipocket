export const CATEGORIES = {
  0: {
    name: '미분류',
    bg: 'bg-label-alternative/10',
    text: 'text-label-alternative',
  },
  1: { name: '거주', bg: 'bg-muted-mauve-50/10', text: 'text-muted-mauve-50' },
  2: { name: '식비', bg: 'bg-dusty-rose-50/10', text: 'text-dusty-rose-50' },
  3: { name: '교통비', bg: 'bg-warm-tan-50/10', text: 'text-warm-tan-50' },
  4: {
    name: '생활',
    bg: 'bg-antique-gold-50/10',
    text: 'text-antique-gold-50',
  },
  5: { name: '여가', bg: 'bg-olive-green-50/10', text: 'text-olive-green-50' },
  6: { name: '쇼핑', bg: 'bg-sea-green-50/10', text: 'text-sea-green-50' },
  7: { name: '통신비', bg: 'bg-blue-grey-50/10', text: 'text-blue-grey-50' },
  8: { name: '학교', bg: 'bg-steel-blue-50/10', text: 'text-steel-blue-50' },
  9: {
    name: '수입',
    bg: 'bg-periwinkle-blue-50/10',
    text: 'text-periwinkle-blue-50',
  },
} as const;

export type CategoryType = keyof typeof CATEGORIES;
export type CategoryName = (typeof CATEGORIES)[keyof typeof CATEGORIES]['name'];

export const getCategoryName = (code: CategoryType): string => {
  return CATEGORIES[code]?.name ?? '알 수 없음';
};
