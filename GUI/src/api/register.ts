import apiClient from "./client";

export type RegisterPayload = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  idProfileDefault?: number;
  customProfile?: string | null;
};

export async function registerApi(payload: RegisterPayload) {
  const res = await apiClient.post("", payload);
  console.log("registerApi loaded");

  return res.data;
}
