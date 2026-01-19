import type { User } from "../types/userTypes";
import apiClient from "./client";
export async function loginApi( email: string,password: string): Promise<User | null> {

  const res = await apiClient.post("login", {// post API
    email,
    password,
  });
  if (res.status !== 200 ) {
    throw new Error(`Login failed with status ${res.status}`);
  }
  //SI RETURN NULL LES CREDENTIALS SONT FAUX
  if (res.data === null) {
    throw new Error("Invalid credentials");
  }
  
  return res.data;
}



