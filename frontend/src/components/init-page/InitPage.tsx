import LocaleSelectModal from '../common/modal/LocaleSelectModal';

const InitPage = () => {
  return (
    <div className="mx-auto flex w-full items-center justify-center bg-gray-200 pt-21">
      <LocaleSelectModal mode="LOCAL" />
    </div>
  );
};

export default InitPage;
