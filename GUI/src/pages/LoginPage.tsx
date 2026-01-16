import { useForm } from "react-hook-form";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
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
        return; // stop here
      }

      //Save session
      localStorage.setItem("session", JSON.stringify({ user }));

      // go to info user  page
 
      navigate("/InfoUserPage ");

    } catch (err) {
      console.error(err);
      setError("Server error, try again later");
    }
  };
 return (
    <div className="login-container">
      <form onSubmit={handleSubmit(onSubmit)}>
        <h2>Login</h2>

        <input
          {...register("email")}
          placeholder="Email"
          type="email"
          required
          disabled={loading}
        />

        <input
          {...register("password")}
          placeholder="Password"
          type="password"
          required
          disabled={loading}
        />

        {error && (
          <div
            style={{
              marginTop: 10,
              padding: 10,
              borderRadius: 8,
              background: "#ffe5e5",
              color: "#b00020",
            }}
          >
            {error}
          </div>
        )}

        <button type="submit" disabled={loading} style={{ marginTop: 12 }}>
          {loading ? "Signing in..." : "Login"}
        </button>

        {/* Create user */}
        <button
          type="button"
          onClick={() => navigate("/register")}
          disabled={loading}
          style={{
            marginTop: 10
          }}
        >
          Create user
        </button>
      </form>
    </div>
  );
}