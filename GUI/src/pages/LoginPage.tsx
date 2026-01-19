import { useForm } from "react-hook-form";
import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { loginApi } from "../api/auth";

type LoginForm = {
  email: string;
  password: string;
};

export default function LoginPage() {
  const { register, handleSubmit } = useForm<LoginForm>();
  const navigate = useNavigate();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const onSubmit = async (data: LoginForm) => {
    setError("");
    setLoading(true);
    try {
      const user = await loginApi(data.email, data.password);
      if (!user) {
        setError("Invalid email or password");
        setLoading(false);
        return;
      }
      localStorage.setItem("session", JSON.stringify({ user }));
      navigate("/infoUser"); // Fixed route case
    } catch (err) {
      console.error(err);
      setError("Server error, try again later");
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-slate-50">
      {/* Left Side - Brand */}
      <div className="hidden lg:flex lg:w-1/2 bg-slate-900 relative items-center justify-center overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-blue-600 to-slate-900 opacity-90" />
        <div className="relative z-10 p-12 text-white max-w-lg">
          <div className="mb-8 w-14 h-14 bg-white/10 backdrop-blur-md rounded-xl flex items-center justify-center border border-white/20">
            <span className="text-2xl font-bold">MA</span>
          </div>
          <h1 className="text-4xl font-bold mb-4">Plan your journey.</h1>
          <p className="text-slate-300 text-lg">
            Manage your itineraries with precision and ease in one single platform.
          </p>
        </div>
      </div>

      {/* Right Side - Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
        <div className="max-w-md w-full bg-white p-8 rounded-lg border border-slate-200 shadow-sm">
          <h2 className="text-2xl font-bold text-slate-900 mb-2">Welcome back</h2>
          <p className="text-sm text-slate-500 mb-8">Please enter your details to sign in.</p>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
              <input
                {...register("email")}
                type="email"
                className="w-full h-10 px-3 rounded-md border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                disabled={loading}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Password</label>
              <input
                {...register("password")}
                type="password"
                className="w-full h-10 px-3 rounded-md border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                disabled={loading}
              />
            </div>

            {error && (
              <div className="p-3 text-sm text-red-600 bg-red-50 rounded-md border border-red-100">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full h-10 bg-slate-900 text-white text-sm font-medium rounded-md hover:bg-slate-800 transition-colors disabled:opacity-70"
            >
              {loading ? "Signing in..." : "Sign in"}
            </button>

            <div className="text-center mt-6">
              <span className="text-sm text-slate-500">Don't have an account? </span>
              <Link to="/register" className="text-sm font-medium text-blue-600 hover:text-blue-700">
                Sign up
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}