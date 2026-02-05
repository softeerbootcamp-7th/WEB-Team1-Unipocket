import { useState } from 'react';

import Button from '@/components/common/Button';
import { type DateRange } from '@/components/common/calendar/Calendar';
import LocaleSelectModal from '@/components/common/modal/LocaleSelectModal';
import { SelectDateContent } from '@/components/common/modal/SelectDateModal';

import { type CountryCode } from '@/data/countryCode';
import { formatDateToString } from '@/lib/utils';

type Step = 'select-country' | 'select-date';

const InitPage = () => {
  const [step, setStep] = useState<Step>('select-country');
  const [selectedCountry, setSelectedCountry] = useState<CountryCode | null>(
    null,
  );
  const [dateRange, setDateRange] = useState<DateRange>({
    startDate: null,
    endDate: null,
  });

  const isDateValid = !!dateRange.startDate && !!dateRange.endDate;

  const handleCountrySelect = (code: CountryCode) => {
    setSelectedCountry(code);
    setStep('select-date');
  };

  const handleDateChange = (start: Date | null, end: Date | null) => {
    setDateRange({ startDate: start, endDate: end });
  };

  const handleDateConfirm = () => {
    // TODO: API Call with country, startDate, endDate
    const formattedStartDate = dateRange.startDate
      ? formatDateToString(dateRange.startDate)
      : undefined;
    const formattedEndDate = dateRange.endDate
      ? formatDateToString(dateRange.endDate)
      : undefined;

    console.log('API Call:', {
      localCountryCode: selectedCountry,
      startDate: formattedStartDate,
      endDate: formattedEndDate,
    });
  };

  const handlePrevButton = () => {
    setSelectedCountry(null);
    setDateRange({ startDate: null, endDate: null });
    setStep('select-country');
  };

  return (
    <div className="mt-12 flex h-screen w-full justify-center">
      {step === 'select-country' && (
        <LocaleSelectModal
          mode="LOCAL"
          onSelect={handleCountrySelect}
          selectedCode={selectedCountry}
        />
      )}

      {step === 'select-date' && (
        <div className="bg-background-normal rounded-modal-20 flex h-screen w-fit flex-col items-center gap-5 px-10.5 py-10">
          <SelectDateContent
            startDate={dateRange.startDate}
            endDate={dateRange.endDate}
            onChange={handleDateChange}
          />
          <div className="flex w-full justify-between">
            <Button variant="outlined" size="lg" onClick={handlePrevButton}>
              이전
            </Button>
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
