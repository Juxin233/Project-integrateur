import { Link, useNavigate } from "react-router-dom";

export default function Navbar() {
  const navigate = useNavigate();

  const handleSignOut = () => {
    // TODO: clear auth token / context
    navigate("/");
  };

  return (
    <header className="w-full bg-white shadow-sm">
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">

        {/* Logo */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gray-200 rounded-lg flex items-center justify-center font-bold text-gray-600">
            LOGO
          </div>
          <span className="text-lg font-semibold">MyApp</span>
        </div>

        {/* Navigation  */}
        <nav className="flex items-center gap-6 text-sm text-gray-600">
        <Link to="/itinerary">Itinerary</Link>


          {/* future links */}
          <button className="hover:text-blue-600">
            Dashboard
          </button>

          <button className="hover:text-blue-600">
            Settings
          </button>

          <button
            onClick={handleSignOut}
            className="ml-4 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition"
          >
            Sign out
          </button>
        </nav>
      </div>
    </header>
  );
}
