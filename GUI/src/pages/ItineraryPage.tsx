import { MapContainer, TileLayer, Polyline, Marker, Popup, useMapEvents } from "react-leaflet";
import { useState } from "react";
import type { LatLngExpression } from "leaflet";
import "leaflet/dist/leaflet.css";
import Navbar from "../components/Navbar";
import { api } from "../api/http";

// Helper component to capture clicks
function MapClickHandler({ onLocationSelect }: { onLocationSelect: (lat: number, lng: number) => void }) {
  useMapEvents({
    click(e) {
      onLocationSelect(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
}

export default function ItineraryPage() {
  const [routePath, setRoutePath] = useState<LatLngExpression[]>([]);
  const [loading, setLoading] = useState(false);
  
  // Visual markers for where the user clicked
  const [startPoint, setStartPoint] = useState<{lat: number, lng: number} | null>(null);
  const [endPoint, setEndPoint] = useState<{lat: number, lng: number} | null>(null);
  
  // Toggles between selecting "Start" or "End"
  const [selectingMode, setSelectingMode] = useState<"start" | "end">("start");

  // The actual IDs used for routing
  const [startId, setStartId] = useState("");
  const [endId, setEndId] = useState("");

  const centerPosition: LatLngExpression = [43.6047, 1.4442];

  // Logic to handle map clicks and snap to backend node
  const handleMapClick = async (lat: number, lng: number) => {
    try {
      // 1. Get the nearest node ID from backend
      const res = await api.get("/route/nearest", {
        params: { lat, lon: lng }
      });
      const snappedId = res.data; // The ID returned by Java

      // 2. Update state based on current mode
      if (selectingMode === "start") {
        setStartPoint({ lat, lng });
        setStartId(String(snappedId));
        setSelectingMode("end"); // Auto-jump to next step
      } else {
        setEndPoint({ lat, lng });
        setEndId(String(snappedId));
      }
    } catch (err) {
      console.error("Failed to snap to node:", err);
      alert("Could not find a road near this point.");
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!startId || !endId) {
      alert("Please select both a start and end point on the map.");
      return;
    }

    setLoading(true);
    setRoutePath([]);
    
    try {
      const res = await api.get("route/dijkstra", {
        params: { start: startId, end: endId }
      });
      
      const path: LatLngExpression[] = res.data.map((node: any) => [
        node.latitude,
        node.longitude
      ]);
      setRoutePath(path);
    } catch (err) {
      console.error("Routing error:", err);
      alert("Could not calculate route.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 flex flex-col gap-6">
        
        <div className="bg-white p-6 rounded-lg border border-slate-200 shadow-sm">
          <div className="flex justify-between items-center mb-4">
            <h1 className="text-xl font-bold text-slate-900">Itinerary Planner</h1>
            
            {/* Mode Switcher */}
            <div className="flex bg-slate-100 rounded-lg p-1">
              <button 
                onClick={() => setSelectingMode("start")}
                className={`px-3 py-1.5 text-xs font-medium rounded-md transition-all ${
                  selectingMode === "start" ? "bg-white text-blue-600 shadow-sm" : "text-slate-500 hover:text-slate-700"
                }`}
              >
                Set Start
              </button>
              <button 
                onClick={() => setSelectingMode("end")}
                className={`px-3 py-1.5 text-xs font-medium rounded-md transition-all ${
                  selectingMode === "end" ? "bg-white text-blue-600 shadow-sm" : "text-slate-500 hover:text-slate-700"
                }`}
              >
                Set End
              </button>
            </div>
          </div>

          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4 items-end">
            <div className="w-full sm:w-auto">
              <label className="block text-xs font-medium text-slate-500 mb-1">Start Node ID</label>
              <input 
                type="text" 
                value={startId}
                readOnly
                className="w-full bg-slate-50 border border-slate-300 rounded-md px-3 py-2 text-sm text-slate-600 cursor-not-allowed"
                placeholder="Click map..."
              />
            </div>
            <div className="w-full sm:w-auto">
              <label className="block text-xs font-medium text-slate-500 mb-1">End Node ID</label>
              <input 
                type="text" 
                value={endId}
                readOnly
                className="w-full bg-slate-50 border border-slate-300 rounded-md px-3 py-2 text-sm text-slate-600 cursor-not-allowed"
                placeholder="Click map..."
              />
            </div>
            <button 
              type="submit" 
              disabled={loading}
              className="w-full sm:w-auto bg-slate-900 text-white px-6 py-2 rounded-md text-sm font-medium hover:bg-slate-800 disabled:opacity-50 transition-colors"
            >
              {loading ? "Calculating..." : "Get Route"}
            </button>
          </form>
        </div>

        <div className="flex-1 bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden min-h-[500px] relative">
          <MapContainer center={centerPosition} zoom={13} className="w-full h-full z-0">
            <TileLayer
              attribution='&copy; OpenStreetMap contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            <MapClickHandler onLocationSelect={handleMapClick} />

            {/* Markers */}
            {startPoint && <Marker position={[startPoint.lat, startPoint.lng]}><Popup>Start Point</Popup></Marker>}
            {endPoint && <Marker position={[endPoint.lat, endPoint.lng]}><Popup>Destination</Popup></Marker>}

            {/* Route */}
            {routePath.length > 0 && (
              <>
                <Polyline positions={routePath} color="#2563eb" weight={5} opacity={0.8} />
                <Marker position={routePath[0]} opacity={0.5} />
                <Marker position={routePath[routePath.length - 1]} opacity={0.5} />
              </>
            )}
          </MapContainer>
        </div>
      </main>
    </div>
  );
}