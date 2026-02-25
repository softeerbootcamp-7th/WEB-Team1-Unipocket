import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import Modal, { type ModalProps } from '@/components/modal/Modal';
import Folder from '@/components/travel-page/folder/Folder';
import UploadBox from '@/components/upload/upload-box/UploadBox';

import {
  useGetTravelImageUrlQuery,
  useGetTravelPresignedUrlMutation,
} from '@/api/travels/query';
import { POLICY } from '@/constants/message';

interface TravelPocketImgModalProps extends Omit<
  ModalProps,
  'children' | 'onAction'
> {
  travelId: number;
  imageKey: string | null;
  onAction?: (imageKey: string) => void;
}

const TravelPocketImgModal = ({
  travelId: _travelId, // eslint-disable-line @typescript-eslint/no-unused-vars
  imageKey,
  onAction,
  onClose,
  ...modalProps
}: TravelPocketImgModalProps) => {
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [uploadedImageKey, setUploadedImageKey] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  // 기존 이미지 presigned view URL 조회
  const { data: existingImageUrlData } = useGetTravelImageUrlQuery(imageKey);
  const existingViewUrl = existingImageUrlData?.presignedUrl ?? null;

  // 새로 업로드한 게 없으면 기존 view URL 사용
  const displayUrl = uploadedImageKey ? previewUrl : existingViewUrl;

  // blob URL 메모리 누수 방지
  const blobUrlRef = useRef<string | null>(null);
  useEffect(() => {
    return () => {
      if (blobUrlRef.current) URL.revokeObjectURL(blobUrlRef.current);
    };
  }, []);

  const { mutateAsync: getPresignedUrl } = useGetTravelPresignedUrlMutation();

  const handleFilesSelected = async (files: File[]) => {
    const file = files[0];
    if (!file) return;

    // 이전 blob URL 해제 후 즉시 로컬 미리보기
    if (blobUrlRef.current) URL.revokeObjectURL(blobUrlRef.current);
    const localUrl = URL.createObjectURL(file);
    blobUrlRef.current = localUrl;
    setPreviewUrl(localUrl);
    setUploadedImageKey(null);

    setIsUploading(true);
    try {
      const { presignedUrl, imageKey: newImageKey } = await getPresignedUrl({
        mimeType: file.type,
      });

      const uploadRes = await fetch(presignedUrl, {
        method: 'PUT',
        headers: { 'Content-Type': file.type },
        body: file,
      });

      if (!uploadRes.ok) throw new Error('S3 upload failed');

      setUploadedImageKey(newImageKey);
    } catch {
      toast.error('이미지 업로드에 실패했어요.');
      setPreviewUrl(null);
      setUploadedImageKey(null);
    } finally {
      setIsUploading(false);
    }
  };

  const handleAction = () => {
    if (uploadedImageKey) {
      onAction?.(uploadedImageKey);
    } else {
      onClose();
    }
  };

  return (
    <Modal
      {...modalProps}
      onClose={onClose}
      onAction={handleAction}
      confirmButton={
        uploadedImageKey && !isUploading
          ? { label: '변경', variant: 'solid' }
          : null
      }
    >
      <div className="flex w-fit flex-col items-center gap-10 p-4">
        <p className="heading2-medium text-label-normal">썸네일 변경</p>
        <div className="flex w-46.5 flex-col items-center gap-2.5">
          <Folder imageKey={displayUrl} />
          <p className="label2-regular text-label-alternative">현재 썸네일</p>
        </div>
        <div className="h-40 w-115">
          <UploadBox
            type="image"
            onFilesSelected={handleFilesSelected}
            message={POLICY.TRAVEL_MODAL}
          />
        </div>
        {isUploading && (
          <p className="body2-normal-medium text-label-alternative animate-pulse">
            업로드 중...
          </p>
        )}
      </div>
    </Modal>
  );
};

export default TravelPocketImgModal;
