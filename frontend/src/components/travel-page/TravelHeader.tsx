import Button from '@/components/common/Button';
import PageHeader from '@/components/layout/PageHeader';

import { PAGE_TITLE } from '@/constants/message';

const TravelHeader = () => {
  return (
    <div className="flex flex-col justify-center gap-11">
      <PageHeader {...PAGE_TITLE.TRAVEL} />
      <div className="flex gap-4">
        <Button variant="solid" size="md">
          여행 포켓 생성하기
        </Button>
        <Button variant="outlined" size="md">
          편집
        </Button>
      </div>
    </div>
  );
};

export default TravelHeader;
