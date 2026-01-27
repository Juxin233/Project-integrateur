import { Link, useNavigate, useLocation } from "react-router-dom";
import { clearSession } from "../api/session";

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();


  const isActive = (path: string) => location.pathname === path;

  const NavItem = ({ to, label }: { to: string; label: string }) => (
    <Link
      to={to}
      className={`px-3 py-2 text-sm font-medium rounded-md transition-colors ${
        isActive(to)
          ? "text-blue-600 bg-blue-50"
          : "text-slate-600 hover:text-slate-900 hover:bg-slate-100"
      }`}
    >
      {label}
    </Link>
  );

  return (
    <header className="sticky top-0 z-50 w-full bg-white/80 backdrop-blur-md border-b border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Logo */}
        <div className="flex items-center gap-3 cursor-pointer" onClick={() => navigate("/infoUser")}>
          <div className="w-8 h-8 bg-blue-600 rounded-md flex items-center justify-center text-white font-bold text-sm">
            MA
          </div>
          <span className="text-lg font-semibold tracking-tight text-slate-900">
            MyApp
          </span>
        </div>

        {/* Navigation */}
        <nav className="hidden md:flex items-center gap-1">
          <NavItem to="/itinerary" label="Itinerary" />
          <NavItem to="/infoUser" label="My account" />
          
          <div className="h-4 w-px bg-slate-200 mx-2" />

          <button
            onClick={() => {
  clearSession();
  navigate("/", { replace: true });
}}
            className="text-sm font-medium text-slate-500 hover:text-red-600 px-3 py-2 transition-colors"
          >
            Sign out
          </button>
        </nav>
      </div>
    </header>
  );
}