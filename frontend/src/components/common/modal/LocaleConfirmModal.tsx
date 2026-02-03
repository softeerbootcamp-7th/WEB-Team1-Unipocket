import clsx from 'clsx';

import type { CountryCode } from '@/data/countryCode';
import { getCountryInfo } from '@/lib/country';

import type { ModalProps } from './Modal';
import Modal from './Modal';

type LocaleChangeSource = 'country' | 'currency';

interface LocaleConfirmModalProps extends Omit<ModalProps, 'children'> {
  type: LocaleChangeSource;
  code: CountryCode;
}

/**
 * LocaleConfirmModal 컴포넌트
 *
 * 국가 또는 통화 변경 확인 모달
 * Modal 컴포넌트로 감싸서 사용합니다.
 */
const LocaleConfirmModal = ({
  type,
  code,
  ...modalProps
}: LocaleConfirmModalProps) => {
  const countryInfo = getCountryInfo(code);

  if (!countryInfo) return null;

  const { imageUrl, countryName, currencySign, currencyName } = countryInfo;
  const isCountry = type === 'country';
  const displayName = isCountry ? countryName : currencyName;

  return (
    <Modal {...modalProps}>
      <div
        className={clsx(
          'text-label-neutral flex flex-col items-center justify-center gap-8',
          isCountry ? 'w-80 py-12.25' : 'w-95 py-25',
        )}
      >
        <div className="flex flex-col items-center gap-4">
          <img
            className="h-15 w-21 object-cover"
            src={imageUrl}
            alt={`${displayName} image`}
          />
          {!isCountry && (
            <span className="heading1-medium">{currencySign}</span>
          )}
        </div>
        <div className="flex w-full flex-col items-center gap-2.5">
          {isCountry ? (
            <>
              <p className="heading2-bold text-center">
                {countryName}(으)로 <br /> 설정하시겠습니까?
              </p>
              <p className="heading2-bold text-label-alternative">
                {currencySign} {currencyName}
              </p>
            </>
          ) : (
            <>
              <p className="heading2-bold text-center">
                기준 통화를 {currencyName}(으)로 <br /> 변경하시겠습니까?
              </p>
              <p className="text-label-alternative body1-normal-regular">
                기존 KRW에서 {currencySign}(으)로 변경됩니다.
              </p>
            </>
          )}
        </div>
      </div>
      {isCountry && (
        <span className="caption1-regular text-label-alternative">
          추후 설정에서 변경할 수 있습니다
        </span>
      )}
    </Modal>
  );
};

export default LocaleConfirmModal;
