import TravelFolderList from '@/components/travel-page/TravelFolderList';
import TravelHeader from '@/components/travel-page/TravelHeader';

const TravelPage = () => {
  return (
    <div className="flex flex-1 flex-col gap-4 px-30 py-8">
      <TravelHeader />
      <TravelFolderList />
    </div>
  );
};

export default TravelPage;
