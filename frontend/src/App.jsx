import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import AppShell from './components/AppShell.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Home from './pages/Home.jsx';
import DriverDashboard from './pages/DriverDashboard.jsx';
import Vehicles from './pages/Vehicles.jsx';
import History from './pages/History.jsx';
import TripDetail from './pages/TripDetail.jsx';
import UserDetail from './pages/UserDetail.jsx';
import Profile from './pages/Profile.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        element={
          <ProtectedRoute>
            <AppShell />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Home />} />
        <Route path="/conducteur" element={<DriverDashboard />} />
        <Route path="/vehicules" element={<Vehicles />} />
        <Route path="/historique" element={<History />} />
        <Route path="/trajets/:id" element={<TripDetail />} />
        <Route path="/users/:id" element={<UserDetail />} />
        <Route path="/profil" element={<Profile />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
