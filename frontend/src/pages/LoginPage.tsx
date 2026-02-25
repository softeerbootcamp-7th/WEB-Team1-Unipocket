import { getRouteApi } from '@tanstack/react-router';

import LoginContainer from '@/components/login-page/LoginContainer';
import LoginContainerTemp from '@/components/login-page/LoginContainerTemp';
import LoginTerms from '@/components/login-page/LoginTerms';
import TextConfirmModal from '@/components/modal/TextModal/TextConfirmModal';

import { API_BASE_URL } from '@/api/config/constants';
import { Icons } from '@/assets';

const routeApi = getRouteApi('/_auth/login');

const LoginPage = () => {
  // 1. URL 파라미터에서 reason과 (혹시 모를) provider를 가져옵니다.
  const { reason, provider } = routeApi.useSearch();

  const navigate = routeApi.useNavigate();

  // 2. reason이 'withdrawn'일 때 모달을 엽니다.
  const isModalOpen = reason === 'withdrawn';

  const handleModalClose = () => {
    navigate({
      to: '.',
      search: {},
      replace: true,
    });
  };

  const handleModalAction = () => {
    // 3. 복구 API 호출 (provider 유무에 따른 분기 처리)
    if (provider) {
      window.location.href = `${API_BASE_URL}/auth/oauth2/reactivate/${provider}`;
    } else {
      // TODO: 백엔드에서 provider 없이 복구가 가능하도록 변경했다면 이 경로를 사용하세요.
      // 변경되지 않았다면 백엔드 개발자에게 URL에 provider 파라미터 추가를 요청해야 합니다.
      window.location.href = `${API_BASE_URL}/auth/oauth2/reactivate`;
    }
  };
  return (
    <main className="realative -mt-14.25 flex min-h-screen items-center justify-center md:static">
      <div className="px-25 py-17.5 text-center">
        <div className="hidden flex-col gap-1 md:mb-25 md:flex">
          <h1 className="text-[32px] font-medium">로그인</h1>
          <h2 className="text-label-alternative">
            소셜 로그인으로 간단하게 시작해보세요
          </h2>
        </div>
        <div className="flex flex-col items-center md:hidden">
          <Icons.Logo width={42} height={42} />
          <Icons.LogoText width={160} height={48} />
          <h1 className="label2-medium text-primary-normal mt-5">
            교환학생 지출의 모든 것, 통합 가계부 유니포켓
          </h1>
        </div>
        <div className="absolute right-20 bottom-23 left-20 flex flex-col gap-3 md:static md:w-81.25">
          <LoginContainer />
          <LoginContainerTemp />
          <LoginTerms />
        </div>
      </div>
      <TextConfirmModal
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onAction={handleModalAction}
        title="계정 복구"
        description="탈퇴한 계정입니다."
        subDescription="계정을 복구하고 다시 로그인하시겠습니까?"
        confirmButton={{ label: '복구하기', variant: 'solid' }} // 기존 삭제 버튼 대신 긍정적인 느낌으로 변경
      />
    </main>
  );
};

export default LoginPage;
