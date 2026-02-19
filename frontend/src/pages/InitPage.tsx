import { useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { toast } from 'sonner';

import { type DateRange } from '@/components/calendar/Calendar';
import Button from '@/components/common/Button';
import LocaleSelectModal from '@/components/modal/LocaleSelectModal';
import { SelectDateContent } from '@/components/modal/SelectDateModal';

import { createAccountBook } from '@/api/account-books/api';
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

  const handleDateChange = ({
    startDate,
    endDate,
  }: {
    startDate: Date | null;
    endDate: Date | null;
  }) => {
    setDateRange({ startDate, endDate });
  };

  const handleDateConfirm = async () => {
    if (!selectedCountry || !dateRange.startDate || !dateRange.endDate) {
      return;
    }
    const formattedStartDate = formatDateToString(dateRange.startDate);
    const formattedEndDate = formatDateToString(dateRange.endDate);

    try {
      await createAccountBook({
        localCountryCode: selectedCountry,
        startDate: formattedStartDate,
        endDate: formattedEndDate,
      });
      navigate({ to: '/home' });
    } catch {
      toast.error('가계부 생성에 실패했어요. 다시 시도해주세요.');
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
