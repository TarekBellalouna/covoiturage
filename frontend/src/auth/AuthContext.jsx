import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import api from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadUser = useCallback(async () => {
    try {
      const { data } = await api.get('/api/users/me');
      setUser(data);
    } catch {
      setUser(null);
    }
  }, []);

  useEffect(() => {
    (async () => {
      if (token) {
        localStorage.setItem('token', token);
        await loadUser();
      } else {
        localStorage.removeItem('token');
        setUser(null);
      }
      setLoading(false);
    })();
  }, [token, loadUser]);

  const login = async (email, motDePasse) => {
    const { data } = await api.post('/api/auth/login', { email, motDePasse });
    setToken(data.token);
  };

  const register = async (payload) => {
    const { data } = await api.post('/api/auth/register', payload);
    setToken(data.token);
  };

  const logout = () => setToken(null);

  return (
    <AuthContext.Provider
      value={{ token, user, loading, login, register, logout, refreshUser: loadUser }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
