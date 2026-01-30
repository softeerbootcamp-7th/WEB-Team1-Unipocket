import { useMatches } from '@tanstack/react-router';

import countryData from '@/datas/country_data.json';
import { countryCode } from '@/datas/countryCode';

interface CountryItemProps {
  flagImg: string;
  country: string;
  currency: string;
  onClick: () => void;
}

const CountryItem = ({
  flagImg,
  country,
  currency,
  onClick,
}: CountryItemProps) => {
  return (
    <div
      className="border-line-normal-normal flex w-full items-center justify-center gap-6 border-b py-5 pr-2.5 pl-2"
      onClick={onClick}
    >
      <div className="bg-dimmer-strong h-5 w-5 rounded-full py-0.5"></div>
      <div className="flex w-full justify-between">
        <div className="flex gap-4">
          <img src={flagImg} alt={`${country} flag`} />
          <span>{country}</span>
        </div>
        <span>{currency}</span>
      </div>
    </div>
  );
};

type LocaleSelectMode = 'BASE' | 'LOCAL'; // 기준 통화, 현지 통화

interface LocaleSelectModalProps {
  mode: LocaleSelectMode;
}

const LocaleSelectModal = ({ mode }: LocaleSelectModalProps) => {
  const matches = useMatches();
  const isInitPath = matches.some((match) => match.routeId === '/_app/init');

  const getTextContent = () => {
    if (mode === 'BASE') {
      return {
        title: '기준 통화 변경',
        subTitle: '선택된 통화로 가계부의 통합 계산 기준이 됩니다',
      };
    }

    // LOCAL mode
    return {
      title: isInitPath ? '교환학생 가는 나라는 어디신가요?' : '국가/통화 변경',
      subTitle: '선택된 나라를 기준으로 현지통화가 설정됩니다',
    };
  };

  const { title, subTitle } = getTextContent();

  return (
    <div className="rounded-modal-20 bg-background-normal flex h-221 w-133 flex-col items-center gap-12 px-7.5 py-13">
      {/* text section */}
      <div className="flex flex-col items-center gap-1.5 text-center">
        <span className="text-label-normal title3-semibold">{title}</span>
        <span className="text-label-assistive heading2-medium">{subTitle}</span>
      </div>

      {/* select section */}
      <div>
        <input type="text" />
        <div className="flex h-170 w-118 flex-col overflow-y-auto">
          {countryCode.map((code) => {
            const data = countryData[code as keyof typeof countryData];

            if (!data) return null;

            return (
              <CountryItem
                key={code}
                flagImg={data.imageUrl}
                country={data.countryName}
                currency={`${data.currencySign} ${data.currencyName}`}
                onClick={() => {
                  console.log(code);
                }}
              />
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default LocaleSelectModal;
