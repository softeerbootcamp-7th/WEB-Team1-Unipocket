export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  expiresIn: number;
  tokenType: string;
}
