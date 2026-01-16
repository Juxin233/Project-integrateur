import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import InfoUserPage from "./pages/InfoUserPage";
import ItineraryPage from "./pages/ItineraryPage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/InfoUserPage" element={<InfoUserPage />} />
        <Route path="/itinerary" element={<ItineraryPage />} />
      </Routes>
    </BrowserRouter>
  );
}

