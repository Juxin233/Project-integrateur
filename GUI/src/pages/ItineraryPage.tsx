import { MapContainer, TileLayer, GeoJSON } from "react-leaflet";
import { useEffect, useState } from "react";
import type { LatLngExpression } from "leaflet"; // Fixed type import
import "leaflet/dist/leaflet.css"; // Fixed by vite-env.d.ts
import Navbar from "../components/Navbar";

export default function ItineraryPage() {
  const [routeData, setRouteData] = useState<any>(null);

  // London coordinates
  const centerPosition: LatLngExpression = [51.5074, -0.1276];

  useEffect(() => {
    // Dynamic import for the GeoJSON file
    import("./test.geojson").then((module) => {
      setRouteData(module.default);
    });
  }, []);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 h-full flex flex-col">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-semibold text-slate-900">Itinerary Planner</h1>
          <button className="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700 shadow-sm transition">
            New Route
          </button>
        </div>

        <div className="flex-1 bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden relative" style={{ minHeight: "600px" }}>
          <MapContainer
            center={centerPosition}
            zoom={13}
            style={{ height: "100%", width: "100%" }}
            className="z-0"
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />
            {routeData && <GeoJSON data={routeData} />}
          </MapContainer>
        </div>
      </main>
    </div>
  );
}