import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { LoginRequest, LoginResponse } from '../models/api';
import {
  AdminUsuario,
  AdminVeiculo,
  AdminVeiculoRequest,
  Dashboard,
  StatusAnuncio
} from '../models/admin';

/**
 * Comunicação HTTP com /api/admin. O token é anexado pelo authInterceptor
 * e todas as permissões são verificadas pelo backend.
 */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/admin`;

  login(payload: LoginRequest): Promise<LoginResponse> {
    return firstValueFrom(this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, payload));
  }

  dashboard(): Promise<Dashboard> {
    return firstValueFrom(this.http.get<Dashboard>(`${this.baseUrl}/dashboard`));
  }

  listarVeiculos(q = '', status = ''): Promise<AdminVeiculo[]> {
    const params: Record<string, string> = {};
    if (q.trim()) params['q'] = q.trim();
    if (status) params['status'] = status;
    return firstValueFrom(this.http.get<AdminVeiculo[]>(`${this.baseUrl}/veiculos`, { params }));
  }

  buscarVeiculo(id: number): Promise<AdminVeiculo> {
    return firstValueFrom(this.http.get<AdminVeiculo>(`${this.baseUrl}/veiculos/${id}`));
  }

  criarVeiculo(payload: AdminVeiculoRequest): Promise<AdminVeiculo> {
    return firstValueFrom(this.http.post<AdminVeiculo>(`${this.baseUrl}/veiculos`, payload));
  }

  atualizarVeiculo(id: number, payload: AdminVeiculoRequest): Promise<AdminVeiculo> {
    return firstValueFrom(this.http.put<AdminVeiculo>(`${this.baseUrl}/veiculos/${id}`, payload));
  }

  alterarStatusVeiculo(id: number, status: StatusAnuncio): Promise<AdminVeiculo> {
    return firstValueFrom(
      this.http.patch<AdminVeiculo>(`${this.baseUrl}/veiculos/${id}/status`, { status })
    );
  }

  excluirVeiculo(id: number): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`${this.baseUrl}/veiculos/${id}`));
  }

  uploadConfigurado(): Promise<{ configurado: boolean }> {
    return firstValueFrom(this.http.get<{ configurado: boolean }>(`${this.baseUrl}/uploads/status`));
  }

  enviarImagem(file: File): Promise<{ url: string }> {
    const form = new FormData();
    form.append('file', file);
    return firstValueFrom(this.http.post<{ url: string }>(`${this.baseUrl}/uploads/imagem`, form));
  }

  listarUsuarios(q = ''): Promise<AdminUsuario[]> {
    const params: Record<string, string> = q.trim() ? { q: q.trim() } : {};
    return firstValueFrom(this.http.get<AdminUsuario[]>(`${this.baseUrl}/usuarios`, { params }));
  }

  alterarStatusUsuario(id: number, ativo: boolean): Promise<AdminUsuario> {
    return firstValueFrom(
      this.http.patch<AdminUsuario>(`${this.baseUrl}/usuarios/${id}/status`, { ativo })
    );
  }

  alterarRoleUsuario(id: number, role: AdminUsuario['role']): Promise<AdminUsuario> {
    return firstValueFrom(
      this.http.patch<AdminUsuario>(`${this.baseUrl}/usuarios/${id}/role`, { role })
    );
  }

  excluirUsuario(id: number): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`${this.baseUrl}/usuarios/${id}`));
  }
}
