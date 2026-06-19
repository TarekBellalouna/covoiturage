import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from '../auth/AuthContext.jsx';

const StompContext = createContext(null);

export function StompProvider({ children }) {
  const { token } = useAuth();
  const clientRef = useRef(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (!token) {
      setConnected(false);
      return;
    }
    const base = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    const client = new Client({
      webSocketFactory: () => new SockJS(`${base}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => setConnected(true),
      onWebSocketClose: () => setConnected(false),
    });
    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [token]);

  // Renvoie une fonction de desabonnement. A appeler dans un effet qui depend de `connected`.
  const subscribe = useCallback((destination, handler) => {
    const client = clientRef.current;
    if (!client || !client.connected) return () => {};
    const sub = client.subscribe(destination, (message) => {
      try {
        handler(JSON.parse(message.body));
      } catch {
        handler(message.body);
      }
    });
    return () => sub.unsubscribe();
  }, []);

  return (
    <StompContext.Provider value={{ connected, subscribe }}>{children}</StompContext.Provider>
  );
}

export function useStomp() {
  return useContext(StompContext);
}
