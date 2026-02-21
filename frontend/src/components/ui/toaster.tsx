import { useTheme } from 'next-themes';
import { Toaster as Sonner, type ToasterProps } from 'sonner';

import { Icons } from '@/assets';

const Toaster = ({ ...props }: ToasterProps) => {
  const { theme = 'system' } = useTheme();

  return (
    <Sonner
      theme={theme as ToasterProps['theme']}
      className="toaster group"
      icons={{
        success: <Icons.CheckmarkCircle className="size-5.5" />,
        error: <Icons.AlertCircle className="size-5.5" />,
      }}
      position="bottom-center"
      offset={40}
      toastOptions={{
        classNames: {
          toast:
            'group-[.toaster]:!flex group-[.toaster]:!items-center group-[.toaster]:!gap-2 group-[.toaster]:!box-border group-[.toaster]:!w-[420px] group-[.toaster]:!px-4 group-[.toaster]:!py-[11px] group-[.toaster]:!body2-normal-bold group-[.toaster]:!text-[#EAEAEB] group-[.toaster]:!bg-[#515253] group-[.toaster]:!border-none group-[.toaster]:!rounded-[12px]',
          title: 'group-[.toaster]:!px-0.5 group-[.toaster]:!py-[5px]',
          icon: 'group-[.toast]:!shrink-0 group-[.toaster]:!w-fit group-[.toaster]:!h-fit group-[.toaster]:!m-0',
        },
      }}
      {...props}
    />
  );
};

export { Toaster };
