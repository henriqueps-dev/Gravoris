export interface User {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  password: string;
  createdAt: string;
}

export interface AuthSession {
  userId: string;
  email: string;
  fullName: string;
  remember: boolean;
}
