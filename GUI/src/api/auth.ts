import type { User } from "../types/userTypes";

export async function loginApi(
  email: string,
  password: string

  //axios.post("/login")
): Promise<User> {
  // simulamos delay de red
  await new Promise((res) => setTimeout(res, 800));

  if (email === "test@test.com" && password === "1234") {
    return {
      id: "1",
      email,
      name: "Usuario Demo",
    };
  }

  throw new Error("Credenciales incorrectas");
}
