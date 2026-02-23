import SelectDateModal from '@/components/modal/SelectDateModal';
import TravelPocketImgModal from '@/components/travel-page/modal/TravelPocketImgModal';
import TravelPocketModal from '@/components/travel-page/modal/TravelPocketModal';
import { useTravelModal } from '@/components/travel-page/modal/useTravelModal';
import TravelFolderList from '@/components/travel-page/TravelFolderList';
import TravelHeader from '@/components/travel-page/TravelHeader';

import { parseStringToDate } from '@/lib/utils';

const TravelPage = () => {
  const {
    activeModal,
    isOpen,
    closeModal,
    openCreateName,
    openCreateDate,
    openEditName,
    openEditDate,
    openDelete,
    openEditThumbnail,
  } = useTravelModal();

  return (
    <div className="flex flex-1 flex-col gap-4 px-30 py-8">
      <TravelHeader onCreateClick={openCreateName} />
      <TravelFolderList
        onOpenEditThumbnail={openEditThumbnail}
        onOpenEditName={openEditName}
        onOpenEditDate={openEditDate}
        onOpenDelete={openDelete}
      />

      {/* 생성 1단계: 포켓명 입력 */}
      <TravelPocketModal
        mode="create"
        isOpen={isOpen && activeModal.type === 'CREATE_NAME'}
        onClose={closeModal}
        onAction={(name) => openCreateDate(name)}
      />

      {/* 생성 2단계: 기간 선택 */}
      <SelectDateModal
        isOpen={isOpen && activeModal.type === 'CREATE_DATE'}
        onClose={closeModal}
        onConfirm={(dateRange) => {
          const travelName =
            activeModal.type === 'CREATE_DATE' ? activeModal.travelName : '';
          console.log('여행 생성 API:', travelName, dateRange);
          closeModal();
        }}
      />

      {/* 폴더명 수정 */}
      <TravelPocketModal
        mode="edit"
        isOpen={isOpen && activeModal.type === 'EDIT_NAME'}
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
        isOpen={isOpen && activeModal.type === 'EDIT_DATE'}
        onClose={closeModal}
        initialDateRange={
          activeModal.type === 'EDIT_DATE'
            ? {
                startDate: parseStringToDate(activeModal.startDate),
                endDate: parseStringToDate(activeModal.endDate),
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
        isOpen={isOpen && activeModal.type === 'DELETE'}
        onClose={closeModal}
        onAction={() => {
          if (activeModal.type === 'DELETE') {
            console.log('삭제 API:', activeModal.travelId);
          }
          closeModal();
        }}
      />

      {/* 썸네일 변경 */}
      {activeModal.type === 'EDIT_THUMBNAIL' && (
        <TravelPocketImgModal
          isOpen={isOpen}
          onClose={closeModal}
          travelId={activeModal.travelId}
          imageUrl={activeModal.imageUrl}
        />
      )}
    </div>
  );
};

export default TravelPage;
