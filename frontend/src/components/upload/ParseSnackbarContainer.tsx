import { useShallow } from 'zustand/react/shallow';

import Snackbar from '@/components/common/Snackbar';
import FileResultModal from '@/components/upload/file-upload/FileResultModal';
import ImageResultModal from '@/components/upload/image-upload/ImageResultModal';

import { useParseSnackbarStore } from '@/stores/parseSnackbarStore';

const ParseSnackbarContainer = () => {
  const { snackbars, closeSnackbar, openResultModal, closeResultModal } =
    useParseSnackbarStore(
      useShallow((state) => ({
        snackbars: state.snackbars,
        closeSnackbar: state.closeSnackbar,
        openResultModal: state.openResultModal,
        closeResultModal: state.closeResultModal,
      })),
    );

  const visibleSnackbars = snackbars.filter((s) => !s.isResultModalOpen);
  const modalSnackbars = snackbars.filter((s) => s.isResultModalOpen);

  return (
    <>
      {/* 스낵바 스택 - 아래에서 위로 쌓임 */}
      {visibleSnackbars.length > 0 && (
        <div className="z-priority fixed bottom-10 left-10 flex flex-col-reverse gap-3">
          {visibleSnackbars.map((entry) => {
            const canOpenResult =
              entry.status === 'success' &&
              entry.parsedMetaId !== undefined &&
              entry.accountBookId !== undefined &&
              entry.parseType !== undefined;

            return (
              <Snackbar
                key={entry.id}
                className="relative bottom-auto left-auto"
                status={entry.status}
                description={entry.description}
                onAction={() => {
                  if (canOpenResult) {
                    openResultModal(entry.id);
                  } else {
                    closeSnackbar(entry.id);
                  }
                }}
              />
            );
          })}
        </div>
      )}

      {/* 결과 모달 */}
      {modalSnackbars.map((entry) => {
        if (
          entry.parsedMetaId === undefined ||
          entry.accountBookId === undefined
        )
          return null;

        if (entry.parseType === 'file') {
          return (
            <FileResultModal
              key={entry.id}
              isOpen
              accountBookId={entry.accountBookId}
              metaId={entry.parsedMetaId}
              onClose={() => closeResultModal(entry.id)}
            />
          );
        }
        if (entry.parseType === 'image') {
          return (
            <ImageResultModal
              key={entry.id}
              isOpen
              accountBookId={entry.accountBookId}
              metaId={entry.parsedMetaId}
              onClose={() => closeResultModal(entry.id)}
            />
          );
        }
        return null;
      })}
    </>
  );
};

export default ParseSnackbarContainer;
