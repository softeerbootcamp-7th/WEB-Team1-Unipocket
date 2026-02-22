export const UPLOAD_STATUS = {
  UPLOADING: 'uploading', // presigned + PUT 진행 중
  UPLOADED: 'uploaded', // S3 업로드 완료 (버튼 활성화 기준)
  PARSING: 'parsing', // parse 요청 후 진행 중
  PARSED: 'parsed', // parse 성공
  ERROR: 'error', // 업로드 or parse 실패
} as const;

export type UploadStatus = (typeof UPLOAD_STATUS)[keyof typeof UPLOAD_STATUS];

export type UploadItem = {
  id: string;
  name: string;
  url?: string; // 업로드 완료 시 존재
  status: UploadStatus;
  taskId?: string;
  s3Key?: string;
};
