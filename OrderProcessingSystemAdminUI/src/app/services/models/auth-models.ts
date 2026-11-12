export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  access_token: string;
  token_type: string;
  expires_in: number;
  refresh_token?: string;
  scope?: string;
}

export interface JwtPayload {
  sub: string;
  email?: string;
  roles?: string[];
  token_use?: string;
  exp: number;
  iat: number;
}

export type UserState = 'ACTIVE' | 'INACTIVE';

export interface UserDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  state: UserState;
  roles: string[];
  createdTime: string;
}

export const ROLE_ADMIN = 'ADMIN';
