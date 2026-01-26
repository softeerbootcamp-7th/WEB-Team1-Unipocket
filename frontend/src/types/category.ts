export type CategoryType =
  | '거주'
  | '식비'
  | '교통비'
  | '생활'
  | '여가'
  | '쇼핑'
  | '통신비'
  | '학교'
  | '수입'
  | '미분류';

export const CATEGORY_STYLE: Record<
  CategoryType,
  {
    bg: string;
    text: string;
  }
> = {
  거주: {
    bg: 'bg-muted-mauve-50/10',
    text: 'text-muted-mauve-50',
  },
  식비: {
    bg: 'bg-dusty-rose-50/10',
    text: 'text-dusty-rose-50',
  },
  교통비: {
    bg: 'bg-warm-tan-50/10',
    text: 'text-warm-tan-50',
  },
  생활: {
    bg: 'bg-antique-gold-50/10',
    text: 'text-antique-gold-50',
  },
  여가: {
    bg: 'bg-olive-green-50/10',
    text: 'text-olive-green-50',
  },
  쇼핑: {
    bg: 'bg-sea-green-50/10',
    text: 'text-sea-green-50',
  },
  통신비: {
    bg: 'bg-blue-grey-50/10',
    text: 'text-blue-grey-50',
  },
  학교: {
    bg: 'bg-steel-blue-50/10',
    text: 'text-steel-blue-50',
  },
  수입: {
    bg: 'bg-periwinkle-blue-50/10',
    text: 'text-periwinkle-blue-50',
  },
  미분류: {
    bg: 'bg-label-alternative/10',
    text: 'text-label-alternative',
  },
};
