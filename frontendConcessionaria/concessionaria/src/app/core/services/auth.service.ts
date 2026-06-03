import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthSession, User } from '../models/user';
import { ToastService } from './toast.service';

const USERS_KEY = 'gravoris-users';
const SESSION_KEY = 'gravoris-session';
const REMEMBER_KEY = 'gravoris-remember';

const DEMO_USER: User = {
  id: 'demo-1',
  fullName: 'Cliente Gravoris',
  email: 'demo@gravoris.com',
  phone: '+55 11 99999-0000',
  password: '123456',
  createdAt: new Date().toISOString()
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly session = signal<AuthSession | null>(this.loadSession());

  readonly currentUser = computed(() => this.session());
  readonly isLoggedIn = computed(() => this.session() !== null);

  readonly userInitial = computed(() => {
    const name = this.session()?.fullName ?? '';
    return name.trim().charAt(0).toUpperCase() || '?';
  });

  constructor(
    private readonly toast: ToastService,
    private readonly router: Router
  ) {
    this.ensureDemoUser();
  }

  login(email: string, password: string, remember = false): boolean {
    const normalized = email.trim().toLowerCase();
    const user = this.getUsers().find(
      (u) => u.email.toLowerCase() === normalized && u.password === password
    );

    if (!user) {
      this.toast.error('E-mail ou senha incorretos');
      return false;
    }

    this.setSession(user, remember);
    this.toast.success('Login realizado com sucesso');
    return true;
  }

  register(data: {
    fullName: string;
    email: string;
    phone: string;
    password: string;
    confirmPassword: string;
  }): boolean {
    if (
      !data.fullName.trim() ||
      !data.email.trim() ||
      !data.phone.trim() ||
      !data.password ||
      !data.confirmPassword
    ) {
      this.toast.error('Verifique os dados informados');
      return false;
    }

    if (data.password !== data.confirmPassword) {
      this.toast.error('Verifique os dados informados');
      return false;
    }

    if (data.password.length < 6) {
      this.toast.error('Verifique os dados informados');
      return false;
    }

    const email = data.email.trim().toLowerCase();
    const users = this.getUsers();

    if (users.some((u) => u.email.toLowerCase() === email)) {
      this.toast.error('Verifique os dados informados');
      return false;
    }

    const user: User = {
      id: crypto.randomUUID?.() ?? `user-${Date.now()}`,
      fullName: data.fullName.trim(),
      email,
      phone: data.phone.trim(),
      password: data.password,
      createdAt: new Date().toISOString()
    };

    this.saveUsers([...users, user]);
    this.setSession(user, true);
    this.toast.success('Conta criada com sucesso');
    return true;
  }

  logout(): void {
    this.session.set(null);
    this.clearStorage();
    void this.router.navigate(['/']);
  }

  private setSession(user: User, remember: boolean): void {
    const session: AuthSession = {
      userId: user.id,
      email: user.email,
      fullName: user.fullName,
      remember
    };
    this.session.set(session);
    this.persistSession(session, remember);
  }

  private ensureDemoUser(): void {
    const users = this.getUsers();
    if (!users.some((u) => u.email === DEMO_USER.email)) {
      this.saveUsers([...users, DEMO_USER]);
    }
  }

  private getUsers(): User[] {
    if (typeof localStorage === 'undefined') return [];
    try {
      const raw = localStorage.getItem(USERS_KEY);
      return raw ? (JSON.parse(raw) as User[]) : [];
    } catch {
      return [];
    }
  }

  private saveUsers(users: User[]): void {
    if (typeof localStorage === 'undefined') return;
    localStorage.setItem(USERS_KEY, JSON.stringify(users));
  }

  private loadSession(): AuthSession | null {
    if (typeof localStorage === 'undefined' && typeof sessionStorage === 'undefined') {
      return null;
    }
    try {
      const remember = localStorage?.getItem(REMEMBER_KEY) === 'true';
      const storage = remember ? localStorage : sessionStorage;
      const raw = storage?.getItem(SESSION_KEY);
      return raw ? (JSON.parse(raw) as AuthSession) : null;
    } catch {
      return null;
    }
  }

  private persistSession(session: AuthSession, remember: boolean): void {
    if (typeof localStorage === 'undefined') return;

    localStorage.setItem(REMEMBER_KEY, String(remember));
    const storage = remember ? localStorage : sessionStorage;
    const other = remember ? sessionStorage : localStorage;

    storage.setItem(SESSION_KEY, JSON.stringify(session));
    other.removeItem(SESSION_KEY);
  }

  private clearStorage(): void {
    if (typeof localStorage === 'undefined') return;
    localStorage.removeItem(SESSION_KEY);
    localStorage.removeItem(REMEMBER_KEY);
    sessionStorage.removeItem(SESSION_KEY);
  }
}

function injectAuthModal(): import('./auth-modal.service').AuthModalService {
  // Lazy import pattern avoided — AuthService callers use AuthModalService directly
  throw new Error('Use AuthModalService directly');
}
