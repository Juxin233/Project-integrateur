import { loginApi } from "../api/auth";
import { type User } from "../types/userTypes";

export async function login(
  email: string,
  password: string
): Promise<User> {
  const user = await loginApi(email, password);
  localStorage.setItem("user", JSON.stringify(user));
  return user;
}

export function logout() {
  localStorage.removeItem("user");
}

export function getCurrentUser(): User | null {
  const user = localStorage.getItem("user");
  return user ? JSON.parse(user) : null;
}
