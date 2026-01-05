import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { authService } from '@/services/authService';
import { getRefreshToken } from '@/services/api';
import type { User, LoginRequest, RegisterRequest, UserRole } from '@/types';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);

  async function checkAuth() {
    try {
      const refreshToken = await getRefreshToken();
      if (refreshToken) {
        const response = await authService.refresh();
        if (response) {
          setUser(response.user);
        }
      }
    } catch {
      // Token invalid, user stays logged out
    } finally {
      setIsLoading(false);
    }
  }

  async function login(data: LoginRequest) {
    const response = await authService.login(data);
    setUser(response.user);
  }

  async function register(data: RegisterRequest) {
    const response = await authService.register(data);
    setUser(response.user);
  }

  async function logout() {
    await authService.logout();
    setUser(null);
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
