import { useState } from 'react';

export type TravelModalState =
  | { type: 'NONE' }
  | { type: 'CREATE_NAME' }
  | { type: 'CREATE_DATE'; travelName: string }
  | { type: 'EDIT_NAME'; travelId: number; defaultName: string }
  | { type: 'EDIT_DATE'; travelId: number; startDate: string; endDate: string }
  | { type: 'DELETE'; travelId: number }
  | { type: 'EDIT_THUMBNAIL'; travelId: number; imageUrl: string | null };

export const useTravelModal = () => {
  const [activeModal, setActiveModal] = useState<TravelModalState>({
    type: 'NONE',
  });

  const [isOpen, setIsOpen] = useState(false);

  const closeModal = () => setIsOpen(false);

  return {
    activeModal,
    isOpen,
    closeModal,
    openCreateName: () => {
      setActiveModal({ type: 'CREATE_NAME' });
      setIsOpen(true);
    },
    openCreateDate: (travelName: string) => {
      setActiveModal({ type: 'CREATE_DATE', travelName });
      setIsOpen(true);
    },
    openEditName: (travelId: number, defaultName: string) => {
      setActiveModal({ type: 'EDIT_NAME', travelId, defaultName });
      setIsOpen(true);
    },
    openEditDate: (travelId: number, startDate: string, endDate: string) => {
      setActiveModal({ type: 'EDIT_DATE', travelId, startDate, endDate });
      setIsOpen(true);
    },
    openDelete: (travelId: number) => {
      setActiveModal({ type: 'DELETE', travelId });
      setIsOpen(true);
    },
    openEditThumbnail: (travelId: number, imageUrl: string | null) => {
      setActiveModal({ type: 'EDIT_THUMBNAIL', travelId, imageUrl });
      setIsOpen(true);
    },
  };
};
