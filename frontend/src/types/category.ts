export const CATEGORIES = {
  0: { name: '미분류' },
  1: { name: '거주' },
  2: { name: '식비' },
  3: { name: '교통비' },
  4: { name: '생활' },
  5: { name: '여가' },
  6: { name: '쇼핑' },
  7: { name: '통신비' },
  8: { name: '학교' },
  9: { name: '수입' },
} as const;

export type CategoryId = keyof typeof CATEGORIES;
export type CategoryType = (typeof CATEGORIES)[CategoryId]['name'];

export interface CategoryStyle {
  bg: string;
  text: string;
}

export const CATEGORY_STYLES: Record<CategoryId, CategoryStyle> = {
  0: { bg: 'bg-label-alternative/10', text: 'text-label-alternative' },
  1: { bg: 'bg-muted-mauve-50/10', text: 'text-muted-mauve-50' },
  2: { bg: 'bg-dusty-rose-50/10', text: 'text-dusty-rose-50' },
  3: { bg: 'bg-warm-tan-50/10', text: 'text-warm-tan-50' },
  4: { bg: 'bg-antique-gold-50/10', text: 'text-antique-gold-50' },
  5: { bg: 'bg-olive-green-50/10', text: 'text-olive-green-50' },
  6: { bg: 'bg-sea-green-50/10', text: 'text-sea-green-50' },
  7: { bg: 'bg-blue-grey-50/10', text: 'text-blue-grey-50' },
  8: { bg: 'bg-steel-blue-50/10', text: 'text-steel-blue-50' },
  9: { bg: 'bg-periwinkle-blue-50/10', text: 'text-periwinkle-blue-50' },
};
