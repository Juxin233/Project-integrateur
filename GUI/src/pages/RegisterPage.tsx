import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";

type RegisterFormData = {
  LastName: string;
  FirstName: string;
  email: string;
  password: string;
};

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<RegisterFormData>();

  const onSubmit = async (data: RegisterFormData) => {
    // API Call placeholder
    await new Promise((res) => setTimeout(res, 800));
    navigate("/");
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm bg-white rounded-lg border border-slate-200 shadow-sm p-8">
        
        <div className="text-center mb-8">
          <h1 className="text-xl font-semibold text-slate-900">Create account</h1>
          <p className="text-sm text-slate-500 mt-1">Start your journey with us</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">First Name</label>
              <input
                type="text"
                className="w-full rounded-md border border-slate-300 text-sm px-3 py-2 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                {...register("FirstName", { required: true })}
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Last Name</label>
              <input
                type="text"
                className="w-full rounded-md border border-slate-300 text-sm px-3 py-2 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                {...register("LastName", { required: true })}
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Email</label>
            <input
              type="email"
              className="w-full rounded-md border border-slate-300 text-sm px-3 py-2 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              {...register("email", { required: "Email required" })}
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Password</label>
            <input
              type="password"
              className="w-full rounded-md border border-slate-300 text-sm px-3 py-2 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              {...register("password", { required: true, minLength: 6 })}
            />
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-slate-900 text-white py-2.5 rounded-md text-sm font-medium hover:bg-slate-800 transition disabled:opacity-50 mt-2"
          >
            {isSubmitting ? "Creating..." : "Sign up"}
          </button>
        </form>

        <p className="mt-6 text-center text-xs text-slate-500">
          Already have an account?{" "}
          <Link to="/" className="text-blue-600 hover:underline font-medium">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}