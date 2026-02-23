import TravelFolderList from '@/components/travel-page/TravelFolderList';
import TravelHeader from '@/components/travel-page/TravelHeader';

const TravelPage = () => {
  return (
    <div className="flex flex-1 flex-col gap-4 px-4 py-8 xl:px-30">
      <TravelHeader />
      <TravelFolderList />
    </div>
  );
};

export default TravelPage;
