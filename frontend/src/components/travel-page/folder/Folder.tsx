import CurrencyAmountDisplay from '@/components/currency/CurrencyAmountDisplay';

import type { CountryCode } from '@/data/country/countryCode';

export interface FolderProps {
  imageKey: string | null;
  localCountryCode?: CountryCode;
  localCountryAmount?: number;
  baseCountryCode?: CountryCode;
  baseCountryAmount?: number;
}

const Folder = ({
  imageKey,
  localCountryCode,
  localCountryAmount,
  baseCountryCode,
  baseCountryAmount,
}: FolderProps) => {
  const pathData =
    'M12.3657 1.52247C12.5283 1.90441 12.9032 2.15234 13.3183 2.15234H21.9217C22.4935 2.15234 22.957 2.61586 22.957 3.18764V20.9647C22.957 21.5365 22.4935 22 21.9217 22H1.03529C0.463516 22 0 21.5365 0 20.9647V1.03529C0 0.463517 0.463517 0 1.03529 0H11.0332C11.4483 0 11.8233 0.247929 11.9858 0.629873L12.3657 1.52247Z';

  return (
    <div className="hover:animate-grow rounded-modal-16 bg-cool-neutral-99 border-line-normal-neutral relative cursor-pointer border p-2 transition-transform">
      <svg
        viewBox="0 0 23 22"
        className="h-full w-full"
        xmlns="http://www.w3.org/2000/svg"
      >
        <defs>
          {/* 마스크 정의 */}
          <clipPath id="folderShapeClip">
            <path d={pathData} />
          </clipPath>

          {/* 그라데이션 정의 */}
          <linearGradient id="imageOverlay" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0.3" stopColor="transparent" />
            <stop offset="1" stopColor="rgba(0,0,0,0.6)" />
          </linearGradient>
        </defs>

        {/* 배경색 레이어 */}
        <rect
          width="100%"
          height="100%"
          className="fill-folder-default/30"
          clipPath="url(#folderShapeClip)"
        />

        {/* 이미지 레이어 */}
        {imageKey && (
          <image
            href={imageKey}
            width="100%"
            height="100%"
            preserveAspectRatio="xMidYMid slice"
            clipPath="url(#folderShapeClip)"
          />
        )}

        {/* 그라데이션 오버레이 */}
        <rect
          width="100%"
          height="100%"
          fill="url(#imageOverlay)"
          clipPath="url(#folderShapeClip)"
        />
      </svg>

      {localCountryCode !== undefined && baseCountryCode !== undefined && (
        <div className="absolute right-5.5 bottom-6 flex flex-col items-end gap-1">
          <CurrencyAmountDisplay
            countryCode={localCountryCode}
            amount={localCountryAmount ?? 0}
            size={'folder_sm'}
            variant="muted"
          />
          <CurrencyAmountDisplay
            countryCode={baseCountryCode}
            amount={baseCountryAmount ?? 0}
            size={'folder_lg'}
            variant="inverse"
          />
        </div>
      )}
    </div>
  );
};

export default Folder;
