import Divider from '@/components/common/Divider';

const LoginTerms = () => {
  return (
    <div className="text-label-assistive flex items-center justify-center gap-4 py-3">
      <a
        href="https://api.unipocket.co.kr/legal/privacy-policy.html"
        target="_blank"
        rel="noopener noreferrer"
        className="underline"
      >
        개인정보처리방침
      </a>
      <Divider style="vertical" className="h-4" />
      <a
        href="https://api.unipocket.co.kr/legal/terms.html"
        target="_blank"
        rel="noopener noreferrer"
        className="underline"
      >
        이용약관
      </a>
    </div>
  );
};

export default LoginTerms;
