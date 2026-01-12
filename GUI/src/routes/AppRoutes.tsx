import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import ProfilePage from "../pages/ProfilePage";
import ItineraryPage from "../pages/ItineraryPage";
// // <Route path="/register" element={<RegisterPage />} />h

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
       <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/itinerary" element={<ItineraryPage />} />
      </Routes>
    </BrowserRouter>
  );
}
