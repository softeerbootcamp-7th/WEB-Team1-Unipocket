import type { useSettingModal } from '@/components/setting-page/modal/useSettingModal';
import { SettingRow } from '@/components/setting-page/SettingLayout';

import type {
  GetAccountBookDetailResponse,
  GetAccountBooksResponse,
} from '@/api/account-books/type';
import { getCountryInfo } from '@/lib/country';

interface AccountBookSettingsFormProps {
  detail: GetAccountBookDetailResponse;
  accountBooks: GetAccountBooksResponse;
  openEditAccountBookName: ReturnType<
    typeof useSettingModal
  >['openEditAccountBookName'];
  openDeleteAccountBook: ReturnType<
    typeof useSettingModal
  >['openDeleteAccountBook'];
  openEditAccountBookPeriod: ReturnType<
    typeof useSettingModal
  >['openEditAccountBookPeriod'];
  openEditBaseCurrency: ReturnType<
    typeof useSettingModal
  >['openEditBaseCurrency'];
  openEditLocalCurrency: ReturnType<
    typeof useSettingModal
  >['openEditLocalCurrency'];
}

const AccountBookSettingsForm = ({
  detail,
  accountBooks,
  openEditAccountBookName,
  openDeleteAccountBook,
  openEditAccountBookPeriod,
  openEditBaseCurrency,
  openEditLocalCurrency,
}: AccountBookSettingsFormProps) => {
  const isMain =
    accountBooks.find((book) => book.accountBookId === detail.accountBookId)
      ?.isMain ?? false;

  const existingTitles = accountBooks
    .filter((book) => book.accountBookId !== detail.accountBookId)
    .map((book) => book.title);

  const formatDate = (dateStr: string) => dateStr.replace(/-/g, '.');
  const periodDisplay = `${formatDate(detail.startDate)} - ${formatDate(detail.endDate)}`;

  const baseCountryInfo = getCountryInfo(detail.baseCountryCode);
  const localCountryInfo = getCountryInfo(detail.localCountryCode);

  const baseCurrencyDisplay = baseCountryInfo
    ? `${baseCountryInfo.currencyNameKor} (${baseCountryInfo.code})`
    : detail.baseCountryCode;
  const localCountryDisplay = localCountryInfo
    ? `${localCountryInfo.countryName} · ${localCountryInfo.currencyNameKor}`
    : detail.localCountryCode;

  return (
    <div className="flex flex-col items-start gap-5">
      <div className="border-line-normal-normal flex w-full flex-col items-start gap-2 border-y px-4 py-4.5">
        <SettingRow
          label="이름 수정"
          value={detail.title}
          onEdit={() =>
            openEditAccountBookName(
              detail.accountBookId,
              detail.title,
              existingTitles,
            )
          }
        />
        <SettingRow
          label="기준 통화 변경"
          value={baseCurrencyDisplay}
          onEdit={() =>
            openEditBaseCurrency(
              detail.accountBookId,
              detail.baseCountryCode,
              detail.localCountryCode,
            )
          }
        />
        <SettingRow
          label="국가/통화 변경"
          value={localCountryDisplay}
          onEdit={() =>
            openEditLocalCurrency(
              detail.accountBookId,
              detail.baseCountryCode,
              detail.localCountryCode,
            )
          }
        />
        <SettingRow
          label="가계부 기간 변경"
          value={periodDisplay}
          onEdit={() =>
            openEditAccountBookPeriod(
              detail.accountBookId,
              detail.startDate,
              detail.endDate,
            )
          }
        />
        <button
          onClick={() =>
            openDeleteAccountBook(detail.accountBookId, detail.title, isMain)
          }
          className="body1-normal-bold text-status-negative py-2.5 text-left"
        >
          {detail.title} 삭제
        </button>
      </div>

      <p className="body2-normal-regular text-label-assistive">
        * 한 가계부 안에, 지출 내역, 기준 통화 설정, 국가 설정, 가계부 기간
        정보가 포함되어 있어요.
        <br />* 두 번째 교환학생을 가거나 가계부를 분리하고 싶다면, 새로운
        가계부를 추가해주세요.
      </p>
    </div>
  );
};

export { AccountBookSettingsForm };
export type { AccountBookSettingsFormProps };
