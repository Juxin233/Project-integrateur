import { MapContainer, TileLayer, GeoJSON } from "react-leaflet";
import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";

import routeGeoJsonRaw from "./test.geojson?raw";

export default function ItineraryPage() {
  const [routeData, setRouteData] = useState<any>(null);

  useEffect(() => {
    try {
      const parsed = JSON.parse(routeGeoJsonRaw);
      setRouteData(parsed);
    } catch (err) {
      console.error("Invalid GeoJSON:", err);
    }
  }, []);

  return (
    <div>
      <Navbar />

      <h1 style={{ padding: 20 }}>Route Planner</h1>

      <MapContainer
        center={[51.5074, -0.1276]}
        zoom={13}
        style={{ height: "500px", width: "100%" }}
      >
        <TileLayer
          attribution="© OpenStreetMap contributors"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {routeData && <GeoJSON data={routeData} />}
      </MapContainer>
    </div>
  );
}
