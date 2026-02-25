import TravelFolderList from '@/components/travel-page/folder/TravelFolderList';
import TravelModalManager from '@/components/travel-page/modal/TravelModalManager';
import { useTravelModal } from '@/components/travel-page/modal/useTravelModal';
import TravelHeader from '@/components/travel-page/TravelHeader';

const TravelPage = () => {
  const {
    activeModal,
    closeModal,
    openCreateName,
    openCreateDate,
    openEditName,
    openEditDate,
    openDelete,
    openEditThumbnail,
  } = useTravelModal();

  return (
    <div className="8 flex flex-1 flex-col gap-4 px-4 py-8 xl:px-30">
      <TravelHeader onCreateClick={openCreateName} />
      <TravelFolderList
        onOpenEditThumbnail={openEditThumbnail}
        onOpenEditName={openEditName}
        onOpenEditDate={openEditDate}
        onOpenDelete={openDelete}
      />

      <TravelModalManager
        activeModal={activeModal}
        closeModal={closeModal}
        openCreateDate={openCreateDate}
      />
    </div>
  );
};

export default TravelPage;
