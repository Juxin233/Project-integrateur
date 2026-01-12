import { MapContainer, TileLayer, GeoJSON } from "react-leaflet";
import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";

export default function ItineraryPage() {
  const [routeData, setRouteData] = useState<any>(null);

  useEffect(() => {
    import("./test.geojson").then((module) => {
      setRouteData(module.default);
    });
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
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {routeData && <GeoJSON data={routeData} />}
      </MapContainer>
    </div>
  );
}
