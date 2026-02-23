import SelectDateModal from '@/components/modal/SelectDateModal';
import TravelPocketImgModal from '@/components/travel-page/modal/TravelPocketImgModal';
import TravelPocketModal from '@/components/travel-page/modal/TravelPocketModal';
import type { TravelModalState } from '@/components/travel-page/modal/useTravelModal';

import {
  useCreateTravelMutation,
  useDeleteTravelMutation,
  usePatchTravelMutation,
} from '@/api/travels/query';
import type { PatchTravelRequest } from '@/api/travels/type';
import { formatDateToString, parseStringToDate } from '@/lib/utils';

interface TravelModalManagerProps {
  activeModal: TravelModalState;
  closeModal: () => void;
  openCreateDate: (travelName: string) => void;
}

const TravelModalManager = ({
  activeModal,
  closeModal,
  openCreateDate,
}: TravelModalManagerProps) => {
  const { type } = activeModal;

  const editTravelId = 'travelId' in activeModal ? activeModal.travelId : 0;

  const createMutation = useCreateTravelMutation();
  const patchMutation = usePatchTravelMutation();
  const deleteMutation = useDeleteTravelMutation();

  const handlePatch = (
    payload: PatchTravelRequest & { travelId: number | string },
  ) => {
    patchMutation.mutate(payload, {
      onSuccess: closeModal,
      onError: closeModal,
    });
  };

  return (
    <>
      {/* 폴더명 관련 (생성/수정/삭제) */}
      <TravelPocketModal
        isOpen={['CREATE_NAME', 'EDIT_NAME', 'DELETE'].includes(type)}
        mode={
          type === 'CREATE_NAME'
            ? 'create'
            : type === 'EDIT_NAME'
              ? 'edit'
              : 'delete'
        }
        initialName={type === 'EDIT_NAME' ? activeModal.defaultName : ''}
        onClose={closeModal}
        onAction={(name) => {
          if (type === 'CREATE_NAME') openCreateDate(name);
          if (type === 'EDIT_NAME')
            handlePatch({ ...activeModal, travelPlaceName: name });
          if (type === 'DELETE')
            deleteMutation.mutate(editTravelId, { onSuccess: closeModal });
        }}
      />

      {/* 기간 선택 관련 (생성/수정) */}
      <SelectDateModal
        isOpen={type === 'CREATE_DATE' || type === 'EDIT_DATE'}
        onClose={closeModal}
        initialDateRange={
          type === 'EDIT_DATE'
            ? {
                startDate: parseStringToDate(activeModal.startDate),
                endDate: parseStringToDate(activeModal.endDate),
              }
            : undefined
        }
        onConfirm={(range) => {
          if (!range.startDate || !range.endDate) return closeModal();
          const dates = {
            startDate: formatDateToString(range.startDate),
            endDate: formatDateToString(range.endDate),
          };

          if (type === 'CREATE_DATE') {
            createMutation.mutate(
              {
                ...dates,
                travelPlaceName: activeModal.travelName,
                imageKey: '',
              },
              { onSuccess: closeModal },
            );
          } else if (type === 'EDIT_DATE') {
            handlePatch({ ...activeModal, ...dates });
          }
        }}
      />

      {/* 썸네일 수정 */}
      {type === 'EDIT_THUMBNAIL' && (
        <TravelPocketImgModal
          isOpen={true}
          onClose={closeModal}
          travelId={activeModal.travelId}
          imageKey={activeModal.imageKey}
        />
      )}
    </>
  );
};

export default TravelModalManager;
