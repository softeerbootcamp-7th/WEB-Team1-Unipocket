import clsx from 'clsx';

import type { ModalProps } from './Modal';
import Modal from './Modal';

type LocaleChangeSource = 'country' | 'currency';

interface LocaleConfirmModalProps extends Omit<ModalProps, 'children'> {
  type: LocaleChangeSource;
  imgUrl: string;
  countryName: string;
  currency: string;
  currencyName: string;
}

/**
 * LocaleConfirmModal 컴포넌트
 *
 * 국가 또는 통화 변경 확인 모달
 * Modal 컴포넌트로 감싸서 사용합니다.
 *
 * 추후에 props로 국가 코드만 받아서 이미지 URL 등을 내부에서 매핑하도록 변경할 수 있습니다.
 */
const LocaleConfirmModal = ({
  type,
  imgUrl,
  countryName,
  currency,
  currencyName,
  ...modalProps
}: LocaleConfirmModalProps) => {
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
            src={imgUrl}
            alt={`${displayName} image`}
          />
          {!isCountry && <span className="heading1-medium">{currency}</span>}
        </div>
        <div className="flex w-full flex-col items-center gap-2.5">
          {isCountry ? (
            <>
              <p className="heading2-bold">
                {countryName}(으)로 설정하시겠습니까?
              </p>
              <p className="heading2-bold text-label-alternative">
                {currency} {currencyName}
              </p>
            </>
          ) : (
            <>
              <p className="heading2-bold">
                기준 통화를 {currencyName}(으)로 변경하시겠습니까?
              </p>
              <p className="text-label-alternative body1-normal-regular">
                기존 KRW에서 {currency}(으)로 변경됩니다.
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
