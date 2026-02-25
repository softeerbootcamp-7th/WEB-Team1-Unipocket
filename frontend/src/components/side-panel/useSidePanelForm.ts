import { useState } from 'react';

import type { CategoryId } from '@/types/category';

function useSidePanelForm() {
  const [title, setTitle] = useState('');
  const [selectedDateTime, setSelectedDateTime] = useState<Date>(
    () => new Date(),
  );
  const [isDateTimePickerOpen, setIsDateTimePickerOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<CategoryId>(0);
  const [selectedCardNumber, setSelectedCardNumber] = useState<string | null>(
    null,
  );
  const [selectedTravelId, setSelectedTravelId] = useState<
    number | string | null
  >(null);

  return {
    title,
    setTitle,
    selectedDateTime,
    setSelectedDateTime,
    isDateTimePickerOpen,
    setIsDateTimePickerOpen,
    selectedCategory,
    setSelectedCategory,
    selectedCardNumber,
    setSelectedCardNumber,
    selectedTravelId,
    setSelectedTravelId,
    resetForm: () => {
      setTitle('');
      setSelectedDateTime(new Date());
      setIsDateTimePickerOpen(false);
      setSelectedCategory(0);
      setSelectedCardNumber(null);
      setSelectedTravelId(null);
    },
  };
}

export default useSidePanelForm;
