import { useState } from 'react';

import Button from '@/components/common/Button';
import { type DateRange } from '@/components/common/calendar/Calendar';
import LocaleSelectModal from '@/components/common/modal/LocaleSelectModal';
import { SelectDateContent } from '@/components/common/modal/SelectDateModal';

import { type CountryCode } from '@/data/countryCode';
import { formatDateToString } from '@/lib/utils';

const InitPage = () => {
  const [step, setStep] = useState<'select-country' | 'select-date'>(
    'select-country',
  );
  const [selectedCountry, setSelectedCountry] = useState<
    CountryCode | undefined
  >();
  const [dateRange, setDateRange] = useState<DateRange>({
    startDate: null,
    endDate: null,
  });
  const [isDateValid, setDateValid] = useState(false);

  const handleCountrySelect = (code: string) => {
    setSelectedCountry(code as CountryCode);
    setStep('select-date');
  };

  const handleDateChange = (start: Date | null, end: Date | null) => {
    setDateRange({ startDate: start, endDate: end });
  };

  const handleDateConfirm = () => {
    const formattedStartDate = dateRange.startDate
      ? formatDateToString(dateRange.startDate)
      : undefined;
    const formattedEndDate = dateRange.endDate
      ? formatDateToString(dateRange.endDate)
      : undefined;

    // TODO: API Call with country, startDate, endDate
    console.log('API Call:', {
      localCountryCode: selectedCountry,
      startDate: formattedStartDate,
      endDate: formattedEndDate,
    });
  };

  return (
    <div className="bg-background-alternative flex h-screen w-full justify-center">
      {step === 'select-country' && (
        <LocaleSelectModal
          mode="LOCAL"
          onSelect={handleCountrySelect}
          selectedCode={selectedCountry}
        />
      )}

      {step === 'select-date' && (
        <div className="bg-background-normal rounded-modal-20 mt-20 flex h-screen w-fit flex-col items-center gap-8 px-10.5 py-10">
          <SelectDateContent
            startDate={dateRange.startDate}
            endDate={dateRange.endDate}
            onChange={handleDateChange}
            onValidChange={setDateValid}
          />
          <div className="flex w-full justify-end">
            <Button
              variant="solid"
              size="lg"
              disabled={!isDateValid}
              onClick={handleDateConfirm}
            >
              가계부 시작하기
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};

export default InitPage;
