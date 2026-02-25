import { getRouteApi } from '@tanstack/react-router';

import LoginContainer from '@/components/login-page/LoginContainer';
import LoginContainerTemp from '@/components/login-page/LoginContainerTemp';
import LoginTerms from '@/components/login-page/LoginTerms';
import TextConfirmModal from '@/components/modal/TextModal/TextConfirmModal';

import { API_BASE_URL } from '@/api/config/constants';
import { Icons } from '@/assets';
import { LOGIN_MODAL_TEXT } from '@/constants/message';

const routeApi = getRouteApi('/_auth/login');

const LoginPage = () => {
  const { reason, provider } = routeApi.useSearch();

  const navigate = routeApi.useNavigate();

  const isModalOpen = reason === 'withdrawn';

  const handleModalClose = () => {
    navigate({
      to: '.',
      search: {},
      replace: true,
    });
  };

  const handleModalAction = () => {
    if (provider) {
      window.location.href = `${API_BASE_URL}/auth/oauth2/reactivate/${provider}`;
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
        title={LOGIN_MODAL_TEXT.REACTIVATE_ACCOUNT.title}
        description={LOGIN_MODAL_TEXT.REACTIVATE_ACCOUNT.description}
        subDescription={LOGIN_MODAL_TEXT.REACTIVATE_ACCOUNT.subDescription}
        confirmButton={{
          label: LOGIN_MODAL_TEXT.REACTIVATE_ACCOUNT.confirmButtonLabel,
          variant: 'solid',
        }}
      />
    </main>
  );
};

export default LoginPage;
