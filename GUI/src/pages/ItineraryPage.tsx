import { MapContainer, TileLayer, GeoJSON } from "react-leaflet";
import { useEffect, useState } from "react";
import type { LatLngExpression } from "leaflet"; 
import "leaflet/dist/leaflet.css";
import Navbar from "../components/Navbar";

// We keep this robust import method you have
import routeGeoJsonRaw from "./test.geojson?raw";

export default function ItineraryPage() {
  const [routeData, setRouteData] = useState<any>(null);

  // London coordinates (default center)
  const centerPosition: LatLngExpression = [51.5074, -0.1276];

  useEffect(() => {
    try {
      const parsed = JSON.parse(routeGeoJsonRaw);
      setRouteData(parsed);
    } catch (err) {
      console.error("Invalid GeoJSON:", err);
    }
  }, []);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 h-full flex flex-col">
        {/* Header Section */}
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Itinerary Planner</h1>
            <p className="text-sm text-slate-500 mt-1">View and manage your travel routes</p>
          </div>
          
          <button className="bg-slate-900 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-slate-800 shadow-sm transition-colors">
            + New Route
          </button>
        </div>

        {/* Map Card */}
        <div className="flex-1 bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden relative min-h-[600px]">
          <MapContainer
            center={centerPosition}
            zoom={13}
            className="w-full h-full z-0"
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            {routeData && <GeoJSON data={routeData} />}
          </MapContainer>
        </div>
      </main>
    </div>
  );
}