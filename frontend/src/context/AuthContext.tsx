import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { User } from '../types';
import AuthService from '../services/authService';

interface AuthContextType {
  user: User | null;
  login: (email: string, password?: string) => Promise<void>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('tua_access_token');
      if (token) {
        try {
          const res = await AuthService.getProfile();
          if (res.success) {
            setUser({
              id: res.data.id,
              email: res.data.email,
              role: res.data.role,
              organizationId: res.data.organizationId,
              verified: res.data.verified,
              approved: res.data.approved
            });
          }
        } catch {
          localStorage.removeItem('tua_access_token');
        }
      }
      setLoading(false);
    };
    checkAuth();
  }, []);

  const login = async (email: string, password?: string) => {
    try {
      const res = await AuthService.login({ email, password });
      if (res.success) {
        localStorage.setItem('tua_access_token', res.data.accessToken);
        const profile = await AuthService.getProfile();
        if (profile.success) {
          setUser({
            id: profile.data.id,
            email: profile.data.email,
            role: profile.data.role,
            organizationId: profile.data.organizationId,
            organizationName: profile.data.organizationName,
            username: profile.data.username,
            phoneNumber: profile.data.phoneNumber,
            website: profile.data.website,
            location: profile.data.location,
            verified: profile.data.verified,
            approved: profile.data.approved
          });
        }
      } else {
        throw new Error(res.message || 'Giriş işlemi başarısız.');
      }
    } catch (error) {
      throw error;
    }
  };

  const logout = async () => {
    try {
      await AuthService.logout();
    } finally {
      setUser(null);
      localStorage.removeItem('tua_access_token');
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
