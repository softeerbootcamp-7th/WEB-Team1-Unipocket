import LocaleSelectModal from '../components/common/modal/LocaleSelectModal';

const InitPage = () => {
  return (
    <div className="bg-background-alternative mx-auto flex w-full items-center justify-center pt-21">
      <LocaleSelectModal mode="LOCAL" />
    </div>
  );
};

export default InitPage;
