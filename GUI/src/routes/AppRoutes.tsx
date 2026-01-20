import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import InfoUser from "../pages/InfoUserPage";
import ItineraryPage from "../pages/ItineraryPage";
import ProtectedRoute from "./ProtectedRoute";
// // <Route path="/register" element={<RegisterPage />} />h

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
       <Route path="/register" element={<RegisterPage />} />
        <Route path="/itinerary" element={<ItineraryPage />} />
      <Route
          path="/InfoUser" element={ <ProtectedRoute>
 <InfoUser/>
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
