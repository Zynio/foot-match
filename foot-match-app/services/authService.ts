import { request, setTokens, clearTokens, getRefreshToken } from './api';
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types';

export const authService = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await request<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    await setTokens(response.accessToken, response.refreshToken);
    return response;
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await request<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    await setTokens(response.accessToken, response.refreshToken);
    return response;
  },

  async refresh(): Promise<AuthResponse | null> {
    const refreshToken = await getRefreshToken();
    if (!refreshToken) return null;

    try {
      const response = await request<AuthResponse>('/api/auth/refresh', {
        method: 'POST',
        body: JSON.stringify({ refreshToken }),
      });
      await setTokens(response.accessToken, response.refreshToken);
      return response;
    } catch {
      await clearTokens();
      return null;
    }
  },

  async logout(): Promise<void> {
    await clearTokens();
  },
};
