import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import type { User } from "../types/userTypes";
import { getInfo } from "../api/infoUser";
import { useNavigate } from "react-router-dom";

type Session = { user: User | User[] };

export default function InfoUserPage() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  
  // Routing Preferences State (Default: 0)
  const [prefs, setPrefs] = useState({
    security: 0,
    comfort: 0,
    difficulty: 0
  });

  const navigate = useNavigate();

  useEffect(() => {
    const raw = localStorage.getItem("session");
    if (!raw) {
      setLoading(false);
      navigate("/", { replace: true });
      return;
    }
    const session: Session = JSON.parse(raw);
    const cachedUser = Array.isArray(session.user) ? session.user[0] : session.user;

    if (cachedUser?.idUser) {
      getInfo(cachedUser.idUser)
        .then((fresh) => setUser(fresh))
        .catch(console.error)
        .finally(() => setLoading(false));
    } else {
      navigate("/");
    }

    // Load saved preferences if they exist
    const savedPrefs = localStorage.getItem("routingPrefs");
    if (savedPrefs) {
      setPrefs(JSON.parse(savedPrefs));
    }
  }, [navigate]);

  const savePreferences = () => {
    localStorage.setItem("routingPrefs", JSON.stringify(prefs));
    alert("Preferences saved! Future routes will use these settings.");
  };

  if (loading) return <div className="min-h-screen bg-slate-50 flex items-center justify-center">Loading...</div>;
  if (!user) return null;

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <div className="max-w-4xl mx-auto px-4 py-10">
        <h1 className="text-2xl font-bold text-slate-900 mb-6">User Profile</h1>
        
        <div className="grid gap-6">
          {/* User Info Card */}
          <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 sm:p-8">
            <h2 className="text-lg font-semibold text-slate-800 mb-4">Account Details</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <Field label="First Name" value={user.firstName} />
              <Field label="Last Name" value={user.lastName} />
              <Field label="Email Address" value={user.email} />
              <Field label="Account ID" value={String(user.idUser)} />
              <Field label="Role" value={user.customProfile || "Standard User"} />
            </div>
          </div>

          {/* Routing Preferences Card */}
          <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 sm:p-8">
            <div className="flex justify-between items-center mb-6">
              <div>
                <h2 className="text-lg font-semibold text-slate-800">Routing Preferences</h2>
                <p className="text-sm text-slate-500">Adjust how the algorithm calculates your path.</p>
              </div>
              <button 
                onClick={savePreferences}
                className="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700 transition-colors"
              >
                Save Preferences
              </button>
            </div>

            <div className="space-y-8">
              {/* Security Slider */}
              <SliderInput 
                label="Security Priority" 
                desc="Prefer safer roads (Lighting, Traffic)" 
                value={prefs.security} 
                onChange={(v) => setPrefs({...prefs, security: v})} 
                color="accent-blue-600"
              />

              {/* Comfort Slider */}
              <SliderInput 
                label="Comfort Priority" 
                desc="Prefer smooth surfaces over speed" 
                value={prefs.comfort} 
                onChange={(v) => setPrefs({...prefs, comfort: v})} 
                color="accent-emerald-600"
              />

              {/* Difficulty Slider */}
              <SliderInput 
                label="Max Difficulty Acceptance" 
                desc="Higher means you accept steeper/harder paths" 
                value={prefs.difficulty} 
                onChange={(v) => setPrefs({...prefs, difficulty: v})} 
                color="accent-purple-600"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className="border-b border-slate-100 pb-2">
      <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1">{label}</p>
      <p className="text-sm font-medium text-slate-900">{value}</p>
    </div>
  );
}

function SliderInput({ label, desc, value, onChange, color }: { label: string, desc: string, value: number, onChange: (v: number) => void, color: string }) {
  return (
    <div>
      <div className="flex justify-between mb-2">
        <div>
          <label className="text-sm font-medium text-slate-900">{label}</label>
          <p className="text-xs text-slate-500">{desc}</p>
        </div>
        <span className="text-sm font-bold text-slate-700 w-8 text-right">{value}</span>
      </div>
      <input 
        type="range" 
        min="0" 
        max="10" 
        step="1"
        value={value} 
        onChange={(e) => onChange(Number(e.target.value))}
        className={`w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer ${color}`}
      />
      <div className="flex justify-between text-xs text-slate-400 mt-1">
        <span>Low</span>
        <span>High</span>
      </div>
    </div>
  );
}