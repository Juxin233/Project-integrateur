import { MapContainer, TileLayer, Polyline, Marker, Popup, useMapEvents } from "react-leaflet";
import { useState, useEffect } from "react";
import type { LatLngExpression } from "leaflet";
import "leaflet/dist/leaflet.css";
import Navbar from "../components/Navbar";
import { api } from "../api/http";

// --- Helper Component to Handle Clicks on the Map ---
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
  
  // Selection State
  const [startPoint, setStartPoint] = useState<{lat: number, lng: number} | null>(null);
  const [endPoint, setEndPoint] = useState<{lat: number, lng: number} | null>(null);
  const [selectingMode, setSelectingMode] = useState<"start" | "end">("start");

  // Input IDs
  const [startId, setStartId] = useState("");
  const [endId, setEndId] = useState("");

  // Algo Selection
  const [algo, setAlgo] = useState<"constrained" | "constrainedAstar">("constrained");
  const [userPrefs, setUserPrefs] = useState({ security: 0, comfort: 0, difficulty: 0 });

  useEffect(() => {
    const saved = localStorage.getItem("routingPrefs");
    if (saved) {
      try {
        setUserPrefs(JSON.parse(saved));
      } catch (e) {
        console.error("Error parsing prefs", e);
      }
    }
  }, []);

  const centerPosition: LatLngExpression = [43.6047, 1.4442]; // Toulouse

  const handleMapClick = async (lat: number, lng: number) => {
    try {
      const res = await api.get("/route/nearest", { params: { lat, lon: lng } });
      const snappedId = res.data; 

      if (selectingMode === "start") {
        setStartPoint({ lat, lng });
        setStartId(String(snappedId));
        setSelectingMode("end");
      } else {
        setEndPoint({ lat, lng });
        setEndId(String(snappedId));
      }
    } catch (err) {
      console.error("Failed to snap:", err);
      if (selectingMode === "start") setStartPoint({ lat, lng });
      else setEndPoint({ lat, lng });
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!startId || !endId) {
      alert("Please set both start and end points.");
      return;
    }

    setLoading(true);
    setRoutePath([]);

    try {
      const endpoint = algo === "constrained" ? "route/constrained" : "route/constrained/astar";
      const params = {
        start: startId,
        end: endId,
        sec: userPrefs.security / 10,
        conf: userPrefs.comfort / 10,
        diff: userPrefs.difficulty / 10
      };

      const res = await api.get(endpoint, { params });
      const nodes = res.data.path || [];
      
      if (nodes.length === 0) {
        alert("No route found matching your preferences.");
      } else {
        if (res.data.constraintsRelaxed) {
          alert("Note: Constraints were relaxed to find a path.");
        }
        const path = nodes.map((node: any) => [node.latitude, node.longitude]);
        setRoutePath(path);
      }
    } catch (err) {
      console.error("Routing error:", err);
      alert("Error calculating route. Check backend logs.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 flex flex-col gap-6">
        
        {/* Controls Card */}
        <div className="bg-white p-6 rounded-lg border border-slate-200 shadow-sm">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
            <div>
              <h1 className="text-xl font-bold text-slate-900">Itinerary Planner</h1>
              <div className="text-xs text-slate-500 mt-1 flex gap-3">
                <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded">Security: <strong>{userPrefs.security}</strong></span>
                <span className="bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded">Comfort: <strong>{userPrefs.comfort}</strong></span>
                <span className="bg-purple-50 text-purple-700 px-2 py-0.5 rounded">Difficulty: <strong>{userPrefs.difficulty}</strong></span>
                <a href="/infoUser" className="text-blue-600 underline ml-1">Edit</a>
              </div>
            </div>
            
            <select 
              value={algo} 
              onChange={(e) => setAlgo(e.target.value as any)}
              className="bg-slate-50 border border-slate-300 text-slate-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block p-2.5 min-w-[200px]"
            >
              <option value="constrained">Constrained Dijkstra</option>
              <option value="constrainedAstar">Constrained A*</option>
            </select>
          </div>

          <div className="flex justify-between items-center mb-4 bg-slate-100 p-2 rounded-lg">
             <div className="flex gap-2">
                <button onClick={() => setSelectingMode("start")} className={`px-3 py-1.5 text-xs font-medium rounded-md ${selectingMode === "start" ? "bg-white text-blue-600 ring-1 ring-slate-200" : "text-slate-500"}`}>Set Start</button>
                <button onClick={() => setSelectingMode("end")} className={`px-3 py-1.5 text-xs font-medium rounded-md ${selectingMode === "end" ? "bg-white text-blue-600 ring-1 ring-slate-200" : "text-slate-500"}`}>Set End</button>
             </div>
             <span className="text-xs text-slate-400 hidden sm:inline">Click map to set points</span>
          </div>

          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4 items-end">
            <input type="text" value={startId} readOnly className="w-full bg-slate-50 border border-slate-300 rounded-md px-3 py-2 text-sm" placeholder="Start Node" />
            <input type="text" value={endId} readOnly className="w-full bg-slate-50 border border-slate-300 rounded-md px-3 py-2 text-sm" placeholder="End Node" />
            
            {/* UPDATED BIG BUTTON */}
            <button 
              type="submit" 
              disabled={loading} 
              className="w-full sm:w-auto bg-slate-900 text-white px-8 py-3 rounded-md text-base font-medium hover:bg-slate-800 disabled:opacity-50 transition-colors whitespace-nowrap min-w-[160px]"
            >
              {loading ? "Calculating..." : "Get Route"}
            </button>
          </form>
        </div>

        {/* Map Container */}
        <div 
          className="bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden w-full relative z-0"
          style={{ height: "600px" }} 
        >
          <MapContainer center={centerPosition} zoom={13} className="w-full h-full">
            <TileLayer attribution='&copy; OpenStreetMap' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            <MapClickHandler onLocationSelect={handleMapClick} />
            {startPoint && <Marker position={[startPoint.lat, startPoint.lng]}><Popup>Start</Popup></Marker>}
            {endPoint && <Marker position={[endPoint.lat, endPoint.lng]}><Popup>End</Popup></Marker>}
            {routePath.length > 0 && <Polyline positions={routePath} color="#2563eb" weight={5} opacity={0.8} />}
          </MapContainer>
        </div>
      </main>
    </div>
  );
}