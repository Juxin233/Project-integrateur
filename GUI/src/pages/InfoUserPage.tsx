import { useEffect, useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import type { User } from "../types/userTypes";
import { replacePassword } from "../api/infoUser";
import { replaceFirstName, replaceLastName, replaceProfileDefault, getInfo } from "../api/infoUser";

import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";

// Types
type Session = { user: User | User[] };

type EditForm = {
  firstName: string;
  lastName: string;
  idProfileDefault: number;
};

type PasswordForm = {
  oldPassword: string;
  newPassword: string;
  confirmNewPassword: string;
};

export default function InfoUser() {
  // --- User & UI State ---
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const [isEditing, setIsEditing] = useState(false);
  const [isPwdOpen, setIsPwdOpen] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // --- Routing Preferences State (From Version 2) ---
  const [prefs, setPrefs] = useState({
    security: 0,
    comfort: 0,
    difficulty: 0
  });

  const navigate = useNavigate();

  // Forms
  const editForm = useForm<EditForm>();
  const pwdForm = useForm<PasswordForm>();

  const roleLabel = useMemo(() => {
    return user ? String(user.idProfileDefault) : "—";
  }, [user]);

  // --- Effects: Load User & Preferences ---
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
        .then((fresh) => {
          setUser(fresh);

          // Pre-load values for editing
          editForm.reset({
            firstName: fresh.firstName,
            lastName: fresh.lastName,
            idProfileDefault: fresh.idProfileDefault ?? 0,
          });
        })
        .catch((e) => {
          console.error(e);
          navigate("/", { replace: true });
        })
        .finally(() => setLoading(false));
    } else {
      navigate("/", { replace: true });
    }

    // Load saved preferences (From Version 2)
    const savedPrefs = localStorage.getItem("routingPrefs");
    if (savedPrefs) {
      try {
        setPrefs(JSON.parse(savedPrefs));
      } catch (e) {
        console.error("Error parsing prefs", e);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [navigate]);

  // --- Handlers ---

  const savePreferences = () => {
    localStorage.setItem("routingPrefs", JSON.stringify(prefs));
    setSuccess("Routing preferences saved successfully.");
    setTimeout(() => setSuccess(""), 3000);
  };

  const onSaveProfile = async (data: EditForm) => {
    if (!user) return;

    setError("");
    setSuccess("");

    try {
      const ops: Promise<any>[] = [];

      const newFirst = data.firstName.trim();
      const newLast = data.lastName.trim();
      const newRole = Number(data.idProfileDefault);

      if (newFirst !== user.firstName) {
        ops.push(replaceFirstName(user.idUser, newFirst));
      }
      if (newLast !== user.lastName) {
        ops.push(replaceLastName(user.idUser, newLast));
      }
      if (newRole !== user.idProfileDefault) {
        ops.push(replaceProfileDefault(user.idUser, newRole));
      }

      if (ops.length === 0) {
        setIsEditing(false);
        setSuccess("No changes to save.");
        return;
      }

      await Promise.all(ops);

      const fresh = await getInfo(user.idUser);
      setUser(fresh);
      localStorage.setItem("session", JSON.stringify({ user: fresh }));

      setIsEditing(false);
      setSuccess("Profile updated successfully.");
    } catch (e) {
      console.error(e);
      setError("Unable to update profile. Please try again.");
    }
  };

  const onChangePassword = async (data: PasswordForm) => {
    if (!user) return;

    if (data.newPassword !== data.confirmNewPassword) {
      pwdForm.setError("confirmNewPassword", { message: "Passwords do not match" });
      return;
    }

    try {
      await replacePassword(user.idUser, data.newPassword);
      setIsPwdOpen(false);
      pwdForm.reset();
      setSuccess("Password changed successfully.");
    } catch (e) {
      console.error(e);
      setError("Unable to change password. Please try again.");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        Loading...
      </div>
    );
  }

  if (!user) return null;

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />

      <div className="max-w-4xl mx-auto px-4 py-10">
        
        {/* Header Section */}
        <div className="flex items-start justify-between gap-4 mb-6">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">User Settings</h1>
            <p className="text-sm text-slate-500">Manage your profile and routing preferences.</p>
          </div>

          <div className="flex gap-2">
            {!isEditing ? (
              <button
                onClick={() => {
                  setSuccess("");
                  setError("");
                  setIsEditing(true);
                }}
                className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 transition-colors"
              >
                Edit profile
              </button>
            ) : (
              <button
                onClick={() => {
                  setIsEditing(false);
                  editForm.reset({
                    firstName: user.firstName,
                    lastName: user.lastName,
                    idProfileDefault: user.idProfileDefault ?? 0,
                  });
                }}
                className="px-4 py-2 rounded-lg border border-slate-300 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 transition-colors"
              >
                Cancel
              </button>
            )}

            <button
              onClick={() => {
                setSuccess("");
                setError("");
                setIsPwdOpen(true);
              }}
              className="px-4 py-2 rounded-lg border border-slate-300 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 transition-colors"
            >
              Change password
            </button>
          </div>
        </div>

        {/* Global Messages */}
        {(error || success) && (
          <div
            className={`mb-4 rounded-lg border px-4 py-3 text-sm ${
              error
                ? "bg-red-50 border-red-200 text-red-700"
                : "bg-green-50 border-green-200 text-green-700"
            }`}
          >
            {error || success}
          </div>
        )}

        <div className="grid gap-8">
          
          {/* CARD 1: Profile Information */}
          <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 sm:p-8">
            <h2 className="text-lg font-semibold text-slate-800 mb-6">Account Details</h2>
            
            {!isEditing ? (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <Field label="First Name" value={user.firstName} />
                <Field label="Last Name" value={user.lastName} />
                <Field label="Email Address" value={user.email} />
                <Field label="Account ID" value={String(user.idUser)} />
                <Field label="Role (idProfileDefault)" value={roleLabel} />
                <Field label="Custom profile" value={user.customProfile ?? "—"} />
              </div>
            ) : (
              <form onSubmit={editForm.handleSubmit(onSaveProfile)} className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="text-xs font-medium text-slate-500 uppercase tracking-wide">First Name</label>
                  <input
                    className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    {...editForm.register("firstName", { required: "First name is required" })}
                  />
                  {editForm.formState.errors.firstName && (
                    <p className="mt-1 text-sm text-red-600">{editForm.formState.errors.firstName.message}</p>
                  )}
                </div>

                <div>
                  <label className="text-xs font-medium text-slate-500 uppercase tracking-wide">Last Name</label>
                  <input
                    className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    {...editForm.register("lastName", { required: "Last name is required" })}
                  />
                  {editForm.formState.errors.lastName && (
                    <p className="mt-1 text-sm text-red-600">{editForm.formState.errors.lastName.message}</p>
                  )}
                </div>

                <div>
                  <label className="text-xs font-medium text-slate-500 uppercase tracking-wide">Email (read-only)</label>
                  <input
                    className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 bg-slate-50 text-slate-500"
                    value={user.email}
                    disabled
                  />
                </div>

                <div>
                  <label className="text-xs font-medium text-slate-500 uppercase tracking-wide">
                    Role (idProfileDefault)
                  </label>
                  <input
                    type="number"
                    className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    {...editForm.register("idProfileDefault", { valueAsNumber: true })}
                  />
                </div>

                <div className="md:col-span-2 flex justify-end gap-2 pt-2">
                  <button
                    type="submit"
                    disabled={editForm.formState.isSubmitting}
                    className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-50"
                  >
                    {editForm.formState.isSubmitting ? "Saving..." : "Save changes"}
                  </button>
                </div>
              </form>
            )}
          </div>

          {/* CARD 2: Routing Preferences */}
          <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 sm:p-8">
            <div className="flex justify-between items-center mb-6">
              <div>
                <h2 className="text-lg font-semibold text-slate-800">Routing Preferences</h2>
                <p className="text-sm text-slate-500">Adjust how the algorithm calculates your path.</p>
              </div>
              <button 
                onClick={savePreferences}
                className="bg-slate-900 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-slate-800 transition-colors"
              >
                Save Preferences
              </button>
            </div>

            <div className="space-y-8">
              <SliderInput 
                label="Security Priority" 
                desc="Prefer safer roads (Lighting, Traffic)" 
                value={prefs.security} 
                onChange={(v) => setPrefs({...prefs, security: v})} 
                color="accent-blue-600"
              />

              <SliderInput 
                label="Comfort Priority" 
                desc="Prefer smooth surfaces over speed" 
                value={prefs.comfort} 
                onChange={(v) => setPrefs({...prefs, comfort: v})} 
                color="accent-emerald-600"
              />

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

        {/* Change Password Modal */}
        {isPwdOpen && (
          <div className="fixed inset-0 bg-black/30 flex items-center justify-center p-4 z-50">
            <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-bold text-slate-900">Change password</h2>
                <button
                  onClick={() => {
                    setIsPwdOpen(false);
                    pwdForm.reset();
                  }}
                  className="text-slate-500 hover:text-slate-900"
                >
                  ✕
                </button>
              </div>

              <form onSubmit={pwdForm.handleSubmit(onChangePassword)} className="space-y-3">
                <div>
                  <input
                    type="password"
                    placeholder="Current password"
                    className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    {...pwdForm.register("oldPassword", { required: "Current password is required" })}
                  />
                  {pwdForm.formState.errors.oldPassword && (
                    <p className="mt-1 text-sm text-red-600">{pwdForm.formState.errors.oldPassword.message}</p>
                  )}
                </div>

                <div>
                  <input
                    type="password"
                    placeholder="New password (min 6 chars)"
                    className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    {...pwdForm.register("newPassword", {
                      required: "New password is required",
                      minLength: { value: 6, message: "Minimum 6 characters" },
                    })}
                  />
                  {pwdForm.formState.errors.newPassword && (
                    <p className="mt-1 text-sm text-red-600">{pwdForm.formState.errors.newPassword.message}</p>
                  )}
                </div>

                <div>
                  <input
                    type="password"
                    placeholder="Confirm new password"
                    className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    {...pwdForm.register("confirmNewPassword", { required: "Please confirm your new password" })}
                  />
                  {pwdForm.formState.errors.confirmNewPassword && (
                    <p className="mt-1 text-sm text-red-600">
                      {pwdForm.formState.errors.confirmNewPassword.message}
                    </p>
                  )}
                </div>

                <div className="flex justify-end gap-2 pt-2">
                  <button
                    type="button"
                    onClick={() => {
                      setIsPwdOpen(false);
                      pwdForm.reset();
                    }}
                    className="px-4 py-2 rounded-lg border border-slate-300 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50"
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    disabled={pwdForm.formState.isSubmitting}
                    className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-50"
                  >
                    {pwdForm.formState.isSubmitting ? "Updating..." : "Update password"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

// --- Helper Components ---

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