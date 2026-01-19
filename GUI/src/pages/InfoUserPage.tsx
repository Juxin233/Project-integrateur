import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import type { User } from "../types/userTypes";
import { getInfo } from "../api/infoUser";
import { useNavigate } from "react-router-dom";

type Session = { user: User | User[] };

export default function InfoUserPage() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
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
  }, [navigate]);

  if (loading) return <div className="min-h-screen bg-slate-50 flex items-center justify-center">Loading...</div>;
  if (!user) return null;

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <div className="max-w-4xl mx-auto px-4 py-10">
        <h1 className="text-2xl font-bold text-slate-900 mb-6">User Profile</h1>
        
        {/* Updated card style to match Login/Register */}
        <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 sm:p-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <Field label="First Name" value={user.firstName} />
            <Field label="Last Name" value={user.lastName} />
            <Field label="Email Address" value={user.email} />
            <Field label="Account ID" value={String(user.idUser)} />
            <Field label="Role" value={user.customProfile || "Standard User"} />
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