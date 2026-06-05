import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ApiErrorResponse, ClienteResponse, LoginResponse } from '../models/api';
import { AuthSession } from '../models/user';
import { ToastService } from './toast.service';

const SESSION_KEY = 'gravoris-session';
const REMEMBER_KEY = 'gravoris-remember';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly session = signal<AuthSession | null>(this.loadSession());

  readonly currentUser = computed(() => this.session());
  readonly isLoggedIn = computed(() => this.session() !== null);

  readonly userInitial = computed(() => {
    const name = this.session()?.fullName ?? '';
    return name.trim().charAt(0).toUpperCase() || '?';
  });

  async login(email: string, password: string, remember = false): Promise<boolean> {
    try {
      const response = await firstValueFrom(
        this.http.post<LoginResponse>(`${API_BASE_URL}/clientes/login`, {
          email: email.trim(),
          password
        })
      );
      this.setSession(response, remember);
      this.toast.success('Login realizado com sucesso');
      return true;
    } catch (error) {
      this.toast.error(this.extractError(error, 'E-mail ou senha incorretos'));
      return false;
    }
  }

  async register(data: {
    fullName: string;
    email: string;
    phone: string;
    cpf: string;
    password: string;
    confirmPassword: string;
  }): Promise<boolean> {
    if (
      !data.fullName.trim() ||
      !data.email.trim() ||
      !data.phone.trim() ||
      !data.cpf.trim() ||
      !data.password ||
      !data.confirmPassword
    ) {
      this.toast.error('Verifique os dados informados');
      return false;
    }

    if (data.password !== data.confirmPassword) {
      this.toast.error('As senhas não coincidem');
      return false;
    }

    if (data.password.length < 6) {
      this.toast.error('A senha deve ter pelo menos 6 caracteres');
      return false;
    }

    try {
      const cliente = await firstValueFrom(
        this.http.post<ClienteResponse>(`${API_BASE_URL}/clientes`, {
          name: data.fullName.trim(),
          email: data.email.trim().toLowerCase(),
          phone: data.phone.trim(),
          cpf: data.cpf.trim(),
          password: data.password
        })
      );

      this.setSession(
        {
          id: cliente.id,
          name: cliente.name,
          email: cliente.email,
          token: ''
        },
        true
      );
      this.toast.success('Conta criada com sucesso');
      return true;
    } catch (error) {
      this.toast.error(this.extractError(error, 'Não foi possível criar a conta'));
      return false;
    }
  }

  logout(): void {
    this.session.set(null);
    this.clearStorage();
    void this.router.navigate(['/']);
  }

  getClienteId(): number | null {
    const id = this.session()?.userId;
    return id ? Number(id) : null;
  }

  private setSession(response: LoginResponse, remember: boolean): void {
    const session: AuthSession = {
      userId: String(response.id),
      email: response.email,
      fullName: response.name,
      token: response.token,
      remember
    };
    this.session.set(session);
    this.persistSession(session, remember);
  }

  private extractError(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as ApiErrorResponse | undefined;
      if (body?.error) return body.error;
    }
    return fallback;
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
