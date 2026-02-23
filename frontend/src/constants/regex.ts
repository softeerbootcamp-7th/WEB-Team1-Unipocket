export const REGEX = {
  // 한글, 영문, 숫자, 특수문자, 공백 허용
  COMMON_TEXT:
    /^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ !@#$%^&*()_+\-=[\]{};':"\\|,.<>/?~`]+$/,

  // 전체가 숫자로만 이루어졌는지 '검사(test)'할 때 사용
  NUMBER_ONLY: /^[0-9]+$/,

  // 입력값에서 숫자가 아닌 문자를 '제거(replace)'할 때 사용
  NON_NUMERIC: /[^0-9]/g,
} as const;
