import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";

// --- FIX 1: Import Leaflet CSS Globally ---
import "leaflet/dist/leaflet.css";

// --- FIX 2: Restore Missing Marker Icons ---
import L from "leaflet";

import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";
import AppRoutes from "./routes/AppRoutes";

// Delete the broken default icon path
delete (L.Icon.Default.prototype as any)._getIconUrl;

// Merge the correct image paths
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AppRoutes />
  </React.StrictMode>,
)