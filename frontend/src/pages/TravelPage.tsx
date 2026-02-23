import TravelModalManager from '@/components/travel-page/modal/TravelModalManager';
import { useTravelModal } from '@/components/travel-page/modal/useTravelModal';
import TravelFolderList from '@/components/travel-page/TravelFolderList';
import TravelHeader from '@/components/travel-page/TravelHeader';

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

      <TravelModalManager
        activeModal={activeModal}
        isOpen={isOpen}
        closeModal={closeModal}
        openCreateDate={openCreateDate}
      />
    </div>
  );
};

export default TravelPage;
