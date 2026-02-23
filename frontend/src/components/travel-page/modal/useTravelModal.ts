import { useState } from 'react';

export type TravelModalState =
  | { type: 'NONE' }
  | { type: 'CREATE_NAME' }
  | { type: 'CREATE_DATE'; travelName: string }
  | {
      type: 'EDIT_NAME';
      travelId: number;
      defaultName: string;
      startDate: string;
      endDate: string;
      imageKey: string;
    }
  | {
      type: 'EDIT_DATE';
      travelId: number;
      startDate: string;
      endDate: string;
      travelPlaceName: string;
      imageKey: string;
    }
  | { type: 'DELETE'; travelId: number }
  | { type: 'EDIT_THUMBNAIL'; travelId: number; imageKey: string | null };

export const useTravelModal = () => {
  const [activeModal, setActiveModal] = useState<TravelModalState>({
    type: 'NONE',
  });

  const isOpen = activeModal.type !== 'NONE';
  const closeModal = () => setActiveModal({ type: 'NONE' });

  return {
    activeModal,
    isOpen,
    closeModal,
    openCreateName: () => {
      setActiveModal({ type: 'CREATE_NAME' });
    },
    openCreateDate: (travelName: string) => {
      setActiveModal({ type: 'CREATE_DATE', travelName });
    },
    openEditName: (
      travelId: number,
      defaultName: string,
      startDate: string,
      endDate: string,
      imageKey: string,
    ) => {
      setActiveModal({
        type: 'EDIT_NAME',
        travelId,
        defaultName,
        startDate,
        endDate,
        imageKey,
      });
    },
    openEditDate: (
      travelId: number,
      startDate: string,
      endDate: string,
      travelPlaceName: string,
      imageKey: string,
    ) => {
      setActiveModal({
        type: 'EDIT_DATE',
        travelId,
        startDate,
        endDate,
        travelPlaceName,
        imageKey,
      });
    },
    openDelete: (travelId: number) => {
      setActiveModal({ type: 'DELETE', travelId });
    },
    openEditThumbnail: (travelId: number, imageKey: string | null) => {
      setActiveModal({ type: 'EDIT_THUMBNAIL', travelId, imageKey });
    },
  };
};
