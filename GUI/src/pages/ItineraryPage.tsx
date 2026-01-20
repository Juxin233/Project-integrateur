import { MapContainer, TileLayer, Polyline, Marker, Popup, useMapEvents } from "react-leaflet";
import { useState, useEffect } from "react";
import type { LatLngExpression } from "leaflet";
import Navbar from "../components/Navbar";
import { api } from "../api/http";

// Helper for clicks
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
  const [startPoint, setStartPoint] = useState<{lat: number, lng: number} | null>(null);
  const [endPoint, setEndPoint] = useState<{lat: number, lng: number} | null>(null);
  const [selectingMode, setSelectingMode] = useState<"start" | "end">("start");
  
  // IDs
  const [startId, setStartId] = useState("");
  const [endId, setEndId] = useState("");

  // Algorithm Selection
  const [algo, setAlgo] = useState<"dijkstra" | "astar" | "constrained" | "constrainedAstar">("dijkstra");

  // Load user preferences for display
  const [userPrefs, setUserPrefs] = useState({ security: 0, comfort: 0, difficulty: 0 });

  useEffect(() => {
    const saved = localStorage.getItem("routingPrefs");
    if (saved) setUserPrefs(JSON.parse(saved));
  }, []);

  const centerPosition: LatLngExpression = [43.6047, 1.4442];

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
    if (!startId || !endId) return alert("Please set both points.");
    setLoading(true);
    setRoutePath([]);

    try {
      let endpoint = "route/dijkstra";
      const params: any = { start: startId, end: endId };

      // Switch endpoints based on selection
      if (algo === "constrained") {
        endpoint = "route/constrained";
        // Backend expects double (e.g. 0.0 to 1.0 or weights). 
        // We divide by 10 since our sliders are 0-10.
        params.sec = userPrefs.security / 10;
        params.conf = userPrefs.comfort / 10;
        params.diff = userPrefs.difficulty / 10;
      } else if (algo === "constrainedAstar") {
        endpoint = "route/constrained/astar";
        params.sec = userPrefs.security / 10;
        params.conf = userPrefs.comfort / 10;
        params.diff = userPrefs.difficulty / 10;
      } else if (algo === "astar") {
        endpoint = "route/astar";
      }

      const res = await api.get(endpoint, { params });
      
      // Handle different response structures (PathResult vs List<Noeud>)
      let nodes = [];
      if (res.data.path) {
        nodes = res.data.path; // It's a PathResult
        if (res.data.constraintsRelaxed) alert("Note: Constraints were relaxed to find a path.");
      } else {
        nodes = res.data; // It's a raw List
      }

      const path = nodes.map((n: any) => [n.latitude, n.longitude] as LatLngExpression);
      setRoutePath(path);

    } catch (err) {
      console.error(err);
      alert("Error calculating route.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 flex flex-col gap-6">
        {/* Controls */}
        <div className="bg-white p-6 rounded-lg border border-slate-200 shadow-sm">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h1 className="text-xl font-bold text-slate-900">Itinerary Planner</h1>
              <div className="text-xs text-slate-500 mt-1 flex gap-2">
                <span>Security: <strong>{userPrefs.security}</strong></span>
                <span>Comfort: <strong>{userPrefs.comfort}</strong></span>
                <span>Difficulty: <strong>{userPrefs.difficulty}</strong></span>
                <a href="/infoUser" className="text-blue-600 underline ml-2">Edit</a>
              </div>
            </div>
            
            {/* Algorithm Selector */}
            <select 
              value={algo} 
              onChange={(e) => setAlgo(e.target.value as any)}
              className="bg-slate-50 border border-slate-300 text-slate-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block p-2.5"
            >
              <option value="dijkstra">Standard Dijkstra</option>
              <option value="astar">Standard A*</option>
              <option value="constrained">Constrained Dijkstra</option>
              <option value="constrainedAstar">Constrained A*</option>
            </select>
          </div>

          <div className="flex justify-between items-center mb-4 bg-slate-100 p-2 rounded-lg">
             <div className="flex gap-2">
                <button onClick={() => setSelectingMode("start")} className={`px-3 py-1.5 text-xs font-medium rounded-md ${selectingMode === "start" ? "bg-white text-blue-600 shadow-sm" : "text-slate-500"}`}>Set Start</button>
                <button onClick={() => setSelectingMode("end")} className={`px-3 py-1.5 text-xs font-medium rounded-md ${selectingMode === "end" ? "bg-white text-blue-600 shadow-sm" : "text-slate-500"}`}>Set End</button>
             </div>
             <span className="text-xs text-slate-400 hidden sm:inline">Click map to set points</span>
          </div>

          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4 items-end">
             <input type="text" value={startId} readOnly className="w-full bg-slate-50 border border-slate-300 rounded-md px-3 py-2 text-sm" placeholder="Start ID" />
             <input type="text" value={endId} readOnly className="w-full bg-slate-50 border border-slate-300 rounded-md px-3 py-2 text-sm" placeholder="End ID" />
             <button type="submit" disabled={loading} className="w-full sm:w-auto bg-slate-900 text-white px-6 py-2 rounded-md text-sm font-medium hover:bg-slate-800 disabled:opacity-50">
               {loading ? "Calculating..." : "Get Route"}
             </button>
          </form>
        </div>

        {/* Map */}
        <div className="bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden h-[600px] w-full relative z-0">
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