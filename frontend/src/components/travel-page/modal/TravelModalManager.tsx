import SelectDateModal from '@/components/modal/SelectDateModal';
import TravelPocketImgModal from '@/components/travel-page/modal/TravelPocketImgModal';
import TravelPocketModal from '@/components/travel-page/modal/TravelPocketModal';
import type { TravelModalState } from '@/components/travel-page/modal/useTravelModal';

import { parseStringToDate } from '@/lib/utils';

interface TravelModalManagerProps {
  activeModal: TravelModalState;
  isOpen: boolean;
  closeModal: () => void;
  openCreateDate: (travelName: string) => void;
}

const TravelModalManager = ({
  activeModal,
  isOpen,
  closeModal,
  openCreateDate,
}: TravelModalManagerProps) => {
  // 열린 모달이 없으면 렌더링하지 않음
  if (!isOpen || activeModal.type === 'NONE') return null;

  return (
    <>
      {/* 생성 1단계: 포켓명 입력 */}
      <TravelPocketModal
        key={activeModal.type === 'CREATE_NAME' ? 'open' : undefined}
        mode="create"
        isOpen={activeModal.type === 'CREATE_NAME'}
        onClose={closeModal}
        onAction={(name) => openCreateDate(name)}
      />

      {/* 생성 2단계: 기간 선택 */}
      <SelectDateModal
        key={activeModal.type === 'CREATE_DATE' ? 'open' : undefined}
        isOpen={activeModal.type === 'CREATE_DATE'}
        onClose={closeModal}
        onConfirm={(dateRange) => {
          // TS 타입 추론: 여기서는 activeModal이 CREATE_DATE 타입임을 보장
          if (activeModal.type === 'CREATE_DATE') {
            console.log('여행 생성 API:', activeModal.travelName, dateRange);
          }
          closeModal();
        }}
      />

      {/* 폴더명 수정 */}
      <TravelPocketModal
        key={
          activeModal.type === 'EDIT_NAME' ? activeModal.travelId : undefined
        }
        mode="edit"
        isOpen={activeModal.type === 'EDIT_NAME'}
        initialName={
          activeModal.type === 'EDIT_NAME' ? activeModal.defaultName : ''
        }
        onClose={closeModal}
        onAction={(name) => {
          if (activeModal.type === 'EDIT_NAME') {
            console.log('이름 수정 API:', activeModal.travelId, name);
          }
          closeModal();
        }}
      />

      {/* 기간 수정 */}
      <SelectDateModal
        key={
          activeModal.type === 'EDIT_DATE' ? activeModal.travelId : undefined
        }
        isOpen={activeModal.type === 'EDIT_DATE'}
        onClose={closeModal}
        initialDateRange={
          activeModal.type === 'EDIT_DATE'
            ? {
                startDate: parseStringToDate(
                  activeModal.startDate.replaceAll('.', '-'),
                ),
                endDate: parseStringToDate(
                  activeModal.endDate.replaceAll('.', '-'),
                ),
              }
            : undefined
        }
        onConfirm={(dateRange) => {
          if (activeModal.type === 'EDIT_DATE') {
            console.log('기간 수정 API:', activeModal.travelId, dateRange);
          }
          closeModal();
        }}
      />

      {/* 폴더 삭제 */}
      <TravelPocketModal
        mode="delete"
        isOpen={activeModal.type === 'DELETE'}
        onClose={closeModal}
        onAction={() => {
          if (activeModal.type === 'DELETE') {
            console.log('삭제 API:', activeModal.travelId);
          }
          closeModal();
        }}
      />

      {/* 썸네일 변경 */}
      <TravelPocketImgModal
        isOpen={activeModal.type === 'EDIT_THUMBNAIL'}
        onClose={closeModal}
        travelId={
          activeModal.type === 'EDIT_THUMBNAIL' ? activeModal.travelId : 0
        }
        imageKey={
          activeModal.type === 'EDIT_THUMBNAIL' ? activeModal.imageKey : null
        }
      />
    </>
  );
};

export default TravelModalManager;
