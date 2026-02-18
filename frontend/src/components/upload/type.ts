export type UploadStatus = 'uploading' | 'done' | 'error';

export type UploadItem = {
  id: string;
  name: string;
  url?: string; // 업로드 완료 시 존재
  status: UploadStatus;
};
