import { useEffect } from 'react';
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Polyline,
  useMap,
  useMapEvents,
} from 'react-leaflet';
import L from 'leaflet';

function pin(color) {
  return L.divIcon({
    className: '',
    html: `<div style="width:18px;height:18px;border-radius:50% 50% 50% 0;transform:rotate(-45deg);background:${color};border:2px solid #fff;box-shadow:0 0 0 1px rgba(0,0,0,.25)"></div>`,
    iconSize: [18, 18],
    iconAnchor: [9, 18],
  });
}

function ClickHandler({ onMapClick }) {
  useMapEvents({
    click(e) {
      onMapClick(e.latlng);
    },
  });
  return null;
}

function Recenter({ center }) {
  const map = useMap();
  useEffect(() => {
    if (center) map.setView(center, map.getZoom());
  }, [center?.[0], center?.[1], map]);
  return null;
}

export default function MapView({
  center = [49.258329, 4.031696],
  zoom = 10,
  markers = [],
  route,
  routes = [],
  tripRoutes = [],
  onMapClick,
}) {
  return (
    <MapContainer center={center} zoom={zoom} style={{ height: '100%', width: '100%' }}>
      <TileLayer
        attribution="&copy; OpenStreetMap"
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {markers.map((m, i) => (
        <Marker
          key={m.id ?? i}
          position={[m.lat, m.lng]}
          icon={pin(m.color || '#0f6e56')}
          eventHandlers={m.onClick ? { click: m.onClick } : undefined}
        >
          {m.label && <Popup>{m.label}</Popup>}
        </Marker>
      ))}
      {routes.map((r, i) => (
        <Polyline key={`r-${i}`} positions={r} color="#ba7517" weight={4} />
      ))}
      {tripRoutes.map((r, i) => (
        <Polyline key={`t-${i}`} positions={r} color="#0f6e56" weight={5} />
      ))}
      {route && route.length >= 2 && <Polyline positions={route} color="#0f6e56" weight={4} />}
      <Recenter center={center} />
      {onMapClick && <ClickHandler onMapClick={onMapClick} />}
    </MapContainer>
  );
}
