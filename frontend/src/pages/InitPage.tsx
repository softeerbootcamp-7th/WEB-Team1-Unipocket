import { useState } from 'react';
import { useNavigate } from '@tanstack/react-router';

import { createAccountBook } from '@/api/account-books/api';

import Button from '@/components/common/Button';
import { type DateRange } from '@/components/common/calendar/Calendar';
import LocaleSelectModal from '@/components/common/modal/LocaleSelectModal';
import { SelectDateContent } from '@/components/common/modal/SelectDateModal';

import { type CountryCode } from '@/data/countryCode';
import { formatDateToString } from '@/lib/utils';

type Step = 'select-country' | 'select-date';

const InitPage = () => {
  const navigate = useNavigate();
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

  const handleDateConfirm = async () => {
    if (!selectedCountry || !dateRange.startDate || !dateRange.endDate) {
      return;
    }
    const formattedStartDate = formatDateToString(dateRange.startDate);
    const formattedEndDate = formatDateToString(dateRange.endDate);

    try {
      const response = await createAccountBook({
        localCountryCode: selectedCountry,
        startDate: formattedStartDate,
        endDate: formattedEndDate,
      });
      console.log('Created:', response);
      navigate({ to: '/home' });
    } catch (error) {
      console.error(error);
    }
  };

  const handlePrevButton = () => {
    setSelectedCountry(null);
    setDateRange({ startDate: null, endDate: null });
    setStep('select-country');
  };

  return (
    <div className="box-border flex h-dvh w-full justify-center pt-12">
      {step === 'select-country' && (
        <LocaleSelectModal
          mode="LOCAL"
          onSelect={handleCountrySelect}
          selectedCode={selectedCountry}
        />
      )}

      {step === 'select-date' && (
        <div className="bg-background-normal rounded-modal-20 flex w-fit flex-col items-center justify-between px-10.5 py-10">
          <SelectDateContent
            startDate={dateRange.startDate}
            endDate={dateRange.endDate}
            onChange={handleDateChange}
          />
          <div className="mb-10 flex w-full items-center justify-between px-4">
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
