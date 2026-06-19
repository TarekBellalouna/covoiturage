import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import { AuthProvider } from './auth/AuthContext.jsx';
import { StompProvider } from './ws/StompProvider.jsx';
import { NotificationProvider } from './ws/NotificationProvider.jsx';
import 'leaflet/dist/leaflet.css';
import './styles.css';

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <StompProvider>
          <NotificationProvider>
            <App />
          </NotificationProvider>
        </StompProvider>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);
