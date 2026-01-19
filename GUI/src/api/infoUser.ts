import apiClient from "./client";
import type { User } from "../types/userTypes";

export async function getInfo(idUser: number): Promise<User>{
 const res = await apiClient.get<User>(`/get/${idUser}`);
 //return res.data[0];
  
  return res.data;
}

