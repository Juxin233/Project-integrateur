import { Navigate } from "react-router-dom";

export default function ProtectedRoute({ children }: { children: JSX.Element }) {
  const session = localStorage.getItem("session"); // Check for session in localStorage
  if (!session) return <Navigate to="/" replace />;
  return children;
}
