import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { useState } from "react";
import { registerApi } from "../api/register";



type RegisterFormData = {
  lastName: string;
  firstName: string;
  email: string;
  password: string;
  confirmPassword: string;
};

export default function RegisterPage() {
  const navigate = useNavigate();
  const [, setServerError] = useState("");

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>();

  const passwordValue = watch("password");

  const onSubmit = async (data: RegisterFormData) => {
    setServerError("");

    try {
      // call register API
      await registerApi({
        firstName: data.firstName.trim(),
        lastName: data.lastName.trim(),
        email: data.email.trim(),
        password: data.password,
        // opcionales si los necesitas luego:
        // idProfileDefault: 0,
        // customProfile: null,
      });

      // after successful registration, navigate to login
      navigate("/");
    } catch (e: any) {
      console.error(e);

      // Generic message 
      setServerError("Unable to create account. Please try again.");
    }
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
                {...register("firstName", { required: true })}
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Last Name</label>
              <input
                type="text"
                className="w-full rounded-md border border-slate-300 text-sm px-3 py-2 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                {...register("lastName", { required: true })}
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Email</label>
            <input
              type="email"
              placeholder="Email address"
              className={`w-full rounded-lg border px-4 py-2 focus:outline-none focus:ring-2 ${
                errors.email
                  ? "border-red-500 focus:ring-red-400"
                  : "border-gray-300 focus:ring-blue-500"
              }`}
              {...register("email", {
                required: "Email is required",
                pattern: {
                  value: /^\S+@\S+\.\S+$/,
                  message: "Invalid email format",
                },
              })}
              disabled={isSubmitting}
            />
            {errors.email && (
              <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Password</label>
            <input
              type="password"
              className="w-full rounded-md border border-slate-300 text-sm px-3 py-2 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              {...register("password", { required: true, minLength: 6 })}
            />
            {errors.password && (
              <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
            )}
          </div>

          {/* Confirm password */}
          <div>
            <input
              type="password"
              placeholder="Confirm password"
              className={`w-full rounded-lg border px-4 py-2 focus:outline-none focus:ring-2 ${
                errors.confirmPassword
                  ? "border-red-500 focus:ring-red-400"
                  : "border-gray-300 focus:ring-blue-500"
              }`}
              {...register("confirmPassword", {
                required: "Please confirm your password",
                validate: (val) =>
                  val === passwordValue || "Passwords do not match",
              })}
              disabled={isSubmitting}
            />
            {errors.confirmPassword && (
              <p className="mt-1 text-sm text-red-600">
                {errors.confirmPassword.message}
              </p>
            )}
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