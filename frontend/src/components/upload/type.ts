export const UPLOAD_STATUS = {
  UPLOADING: 'uploading',
  DONE: 'done',
  ERROR: 'error',
} as const;

export type UploadStatus = (typeof UPLOAD_STATUS)[keyof typeof UPLOAD_STATUS];

export type UploadItem = {
  id: string;
  name: string;
  url?: string; // 업로드 완료 시 존재
  status: UploadStatus;
};
