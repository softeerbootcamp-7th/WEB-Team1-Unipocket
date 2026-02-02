import LocaleSelectModal from '../components/common/modal/LocaleSelectModal';

const InitPage = () => {
  return (
    <div className="bg-background-alternative flex h-screen w-full justify-center">
      <LocaleSelectModal mode="LOCAL" />
    </div>
  );
};

export default InitPage;
