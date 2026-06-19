import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import api from '../api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useStomp } from './StompProvider.jsx';

const NotificationContext = createContext(null);

export function NotificationProvider({ children }) {
  const { user } = useAuth();
  const { connected, subscribe } = useStomp();
  const [items, setItems] = useState([]);
  const [unread, setUnread] = useState(0);

  const charger = useCallback(() => {
    api
      .get('/api/notifications')
      .then(({ data }) => setItems(data))
      .catch(() => {});
    api
      .get('/api/notifications/non-lues')
      .then(({ data }) => setUnread(data.nombre ?? 0))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!user) {
      setItems([]);
      setUnread(0);
      return;
    }
    charger();
  }, [user, charger]);

  useEffect(() => {
    if (!connected) return;
    const unsub = subscribe('/user/queue/notifications', (n) => {
      setItems((prev) => [n, ...prev].slice(0, 50));
      setUnread((c) => c + 1);
    });
    return unsub;
  }, [connected, subscribe]);

  const marquerToutLu = useCallback(async () => {
    try {
      await api.post('/api/notifications/lu-tout');
    } catch {
      /* ignore */
    }
    setItems((prev) => prev.map((n) => ({ ...n, lu: true })));
    setUnread(0);
  }, []);

  return (
    <NotificationContext.Provider value={{ items, unread, marquerToutLu }}>
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  return useContext(NotificationContext);
}
