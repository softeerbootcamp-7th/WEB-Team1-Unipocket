import { useState } from 'react';

import Button from '@/components/common/Button';
import ImageResultModal from '@/components/upload/image-upload/ImageResultModal';
const TravelPage = () => {
  const [isOpen, setIsOpen] = useState(false); // 테스트용

  return (
    <div className="p-10">
      <Button onClick={() => setIsOpen(true)}>결과 모달 열기</Button>

      <ImageResultModal
        isOpen={isOpen}
        imageCount={6}
        expenseCount={13}
        onClose={() => setIsOpen(false)}
        onConfirm={() => {
          console.log('지출 적용');
          setIsOpen(false);
        }}
      />
    </div>
  );
};

export default TravelPage;
