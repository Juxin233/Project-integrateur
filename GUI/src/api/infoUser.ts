import apiClient from "./client";
import type { User } from "../types/userTypes";

export async function getInfo(idUser: number): Promise<User> {
  const res = await apiClient.get<User>(`/get/${idUser}`);
  return res.data;
}

// Helpers para enviar texto plano / número
async function putText(url: string, value: string): Promise<string> {
  const res = await apiClient.put(url, value, {
    headers: { "Content-Type": "text/plain" },
  });
  return res.data;
}

async function putNumber(url: string, value: number): Promise<string> {
  const res = await apiClient.put(url, value, {
    headers: { "Content-Type": "application/json" }, // número válido como JSON
  });
  return res.data;
}

// ---- PUT field-by-field endpoints ----
export function replaceFirstName(idUser: number, firstName: string) {
  return putText(`/replace/fName/${idUser}`, firstName);
}

export function replaceLastName(idUser: number, lastName: string) {
  return putText(`/replace/lName/${idUser}`, lastName);
}

export function replacePassword(idUser: number, password: string) {
  return putText(`/replace/password/${idUser}`, password);
}

export function replaceProfileDefault(idUser: number, idProfileDefault: number) {
  return putNumber(`/replace/profileDefault/${idUser}`, idProfileDefault);
}

export function replaceCustomProfile(idUser: number, customProfile: string) {
  return putText(`/replace/customProfile/${idUser}`, customProfile);
}

// (Email no se cambia en UI, pero si algún día lo necesitas)
// export function replaceEmail(idUser: number, email: string) {
//   return putText(`/user/replace/email/${idUser}`, email);
// }
