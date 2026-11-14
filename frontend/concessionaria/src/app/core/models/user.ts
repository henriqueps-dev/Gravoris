import { UserRole } from './api';

export interface AuthSession {
  userId: string;
  email: string;
  fullName: string;
  token: string;
  remember: boolean;
  role: UserRole;
  /** Expiração do token em ISO 8601 */
  expiresAt: string;
}
