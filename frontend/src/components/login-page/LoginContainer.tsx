import { clsx } from 'clsx';

import { AUTH_PROVIDERS } from '@/constants/authProviders';

interface LoginButtonProps {
  text: string;
  bgColor: string;
  textColor: string;
  Icon: React.ComponentType<{ className: string }>;
  href: string;
}

const LoginButton = ({
  text,
  bgColor,
  textColor,
  Icon,
  href,
}: LoginButtonProps) => (
  <a
    href={href}
    className={clsx(
      'flex items-center justify-center gap-3.5 rounded-lg py-[11.5px] transition-opacity hover:opacity-80',
      bgColor,
      textColor,
    )}
  >
    <Icon className="size-4.5" />
    <span className="text-[15px] font-semibold">{text}</span>
  </a>
);

const LoginContainer = () => {
  return (
    <>
      {AUTH_PROVIDERS.map((provider) => (
        <LoginButton key={provider.id} {...provider} />
      ))}
    </>
  );
};

export default LoginContainer;
