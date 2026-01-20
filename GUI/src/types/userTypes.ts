export interface User {
  idUser: number;
  firstName: string;
  lastName: string;
  idProfileDefault?: number;
  customProfile?: string;
  email: string;
}
