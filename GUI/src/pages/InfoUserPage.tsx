import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import type { User } from "../types/userTypes";
import { getInfo } from "../api/infoUser";
import { useNavigate } from "react-router-dom";

type Session = {
  user: User | User[]; //
};

export default function InfoUserPage() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const raw = localStorage.getItem("session");

    // if no session, go to login
    if (!raw) {
      setLoading(false);
      navigate("/", { replace: true });
      return;
    }

    const session: Session = JSON.parse(raw);

    // if session user is array, take first element
    const cachedUser = Array.isArray(session.user)
      ? session.user[0]
      : session.user;

    // if I already have full data, use cache
    const hasFull =
      cachedUser &&
      typeof cachedUser.idUser === "number" &&
      typeof cachedUser.firstName === "string" &&
      typeof cachedUser.lastName === "string" &&
      typeof cachedUser.email === "string";

    if (hasFull) {
      setUser(cachedUser);
      setLoading(false);
      return;
    }

    // If I have at least an id, fetch fresh data
    if (cachedUser?.idUser) {
      getInfo(cachedUser.idUser)
        .then((fresh) => {
          setUser(fresh);
          localStorage.setItem("session", JSON.stringify({ user: fresh }));
        })
        .finally(() => setLoading(false));
      return;
    }

    // no valid user, go to login
    setLoading(false);
    navigate("/", { replace: true });
  }, [navigate]);

  if (loading) return <div className="p-8">Loading user info...</div>;
  if (!user) return <div className="p-8">No user data</div>;

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />

      <div className="max-w-3xl mx-auto mt-10 bg-white rounded-2xl shadow p-8">
        <h1 className="text-2xl font-bold mb-6">User Info</h1>

        <div className="space-y-4">
          <Field label="First name" value={user.firstName} />
          <Field label="Last name" value={user.lastName} />
          <Field label="Email" value={user.email} />
          <Field label="User ID" value={String(user.idUser)} />
          <Field label="Default profile ID" value={String(user.idProfileDefault)} />
          <Field label="Custom profile" value={user.customProfile ?? "—"} />
        </div>
      </div>
    </div>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-lg font-medium">{value}</p>
    </div>
  );
}
