export const REGEX = {
  // 한글, 영문, 숫자, 특수문자, 공백 허용
  COMMON_TEXT:
    /^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ !@#$%^&*()_+\-=[\]{};':"\\|,.<>/?~`]+$/,

  NUMBER_ONLY: /^[0-9]+$/,
} as const;
