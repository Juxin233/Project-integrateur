import { useNavigate, Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { login } from "../services/authService";

type LoginFormData = {
  email: string;
  password: string;
};

export default function LoginPage() {
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginFormData>();

  const onSubmit = async (data: LoginFormData) => {
    try {
      await login(data.email, data.password);
      navigate("/profile");
    } catch (err: any) {
      alert(err.message);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm bg-white rounded-lg border border-slate-200 shadow-sm p-8">
        
        <div className="text-center mb-8">
          <div className="mx-auto w-12 h-12 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold text-xl mb-4">
            MA
          </div>
          <h1 className="text-xl font-semibold text-slate-900">Welcome back</h1>
          <p className="text-sm text-slate-500 mt-1">Enter your details to sign in</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Email</label>
            <input
              type="email"
              className={`w-full rounded-md border text-sm px-3 py-2 outline-none transition-all ${
                errors.email
                  ? "border-red-500 focus:ring-2 focus:ring-red-100"
                  : "border-slate-300 focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              }`}
              {...register("email", { required: "Email is required" })}
            />
            {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email.message}</p>}
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Password</label>
            <input
              type="password"
              className={`w-full rounded-md border text-sm px-3 py-2 outline-none transition-all ${
                errors.password
                  ? "border-red-500 focus:ring-2 focus:ring-red-100"
                  : "border-slate-300 focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              }`}
              {...register("password", { required: "Password required" })}
            />
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-slate-900 text-white py-2.5 rounded-md text-sm font-medium hover:bg-slate-800 transition disabled:opacity-50 mt-2"
          >
            {isSubmitting ? "Signing in..." : "Sign in"}
          </button>
        </form>

        <p className="mt-6 text-center text-xs text-slate-500">
          Don’t have an account?{" "}
          <Link to="/register" className="text-blue-600 hover:underline font-medium">
            Sign up
          </Link>
        </p>
      </div>
    </div>
  );
}