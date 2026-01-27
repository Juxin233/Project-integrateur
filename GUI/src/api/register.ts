import apiClient from "./client";

export type RegisterPayload = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  idProfileDefault?: number;
  customProfile?: string | null;
};

// The 'export' keyword here is crucial
export async function registerApi(payload: RegisterPayload) {
  // Assuming your backend accepts a POST to the base URL configured in client.ts
  const res = await apiClient.post("", payload); 
  return res.data;
}