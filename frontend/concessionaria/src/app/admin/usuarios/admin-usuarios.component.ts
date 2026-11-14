import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminUsuario } from '../../core/models/admin';
import { AdminService } from '../../core/services/admin.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { adminErrorMessage } from '../shared/admin-error';
import { ConfirmDialogComponent, ConfirmRequest } from '../shared/confirm-dialog.component';

type Filtro = 'todos' | 'clientes' | 'admins' | 'inativos';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [FormsModule, ConfirmDialogComponent],
  templateUrl: './admin-usuarios.component.html',
  styleUrl: './admin-usuarios.component.css'
})
export class AdminUsuariosComponent implements OnInit {
  private readonly admin = inject(AdminService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  protected readonly todos = signal<AdminUsuario[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly filtro = signal<Filtro>('todos');
  protected readonly busyId = signal<number | null>(null);
  protected readonly confirmacao = signal<ConfirmRequest | null>(null);
  protected readonly confirmando = signal(false);
  protected busca = '';

  protected readonly meuId = computed(() => this.auth.getClienteId());

  protected readonly filtros: { value: Filtro; label: string }[] = [
    { value: 'todos', label: 'Todos' },
    { value: 'clientes', label: 'Clientes' },
    { value: 'admins', label: 'Administradores' },
    { value: 'inativos', label: 'Inativos' }
  ];

  protected readonly contagem = computed(() => {
    const l = this.todos();
    return {
      todos: l.length,
      clientes: l.filter((u) => u.role === 'CLIENTE').length,
      admins: l.filter((u) => u.role === 'ADMINISTRADOR').length,
      inativos: l.filter((u) => !u.ativo).length
    } as Record<Filtro, number>;
  });

  protected readonly visiveis = computed(() => {
    const l = this.todos();
    switch (this.filtro()) {
      case 'clientes':
        return l.filter((u) => u.role === 'CLIENTE');
      case 'admins':
        return l.filter((u) => u.role === 'ADMINISTRADOR');
      case 'inativos':
        return l.filter((u) => !u.ativo);
      default:
        return l;
    }
  });

  async ngOnInit(): Promise<void> {
    await this.carregar();
  }

  async carregar(): Promise<void> {
    this.loading.set(true);
    this.error.set(false);
    try {
      this.todos.set(await this.admin.listarUsuarios(this.busca));
    } catch {
      this.error.set(true);
    } finally {
      this.loading.set(false);
    }
  }

  formatCpf(cpf: string): string {
    const d = (cpf || '').replace(/\D/g, '');
    return d.length === 11 ? `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}` : cpf;
  }

  formatData(value: string | null): string {
    if (!value) return '—';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '—' : date.toLocaleDateString('pt-BR');
  }

  private substituir(u: AdminUsuario): void {
    this.todos.update((lista) => lista.map((x) => (x.id === u.id ? u : x)));
  }

  pedirStatus(u: AdminUsuario): void {
    const ativar = !u.ativo;
    this.confirmacao.set({
      title: ativar ? 'Reativar conta' : 'Desativar conta',
      message: ativar
        ? `${u.name} poderá voltar a entrar na plataforma.`
        : `${u.name} será desconectado imediatamente e não poderá mais entrar até a conta ser reativada. Os pedidos e dados são preservados.`,
      confirmLabel: ativar ? 'Reativar' : 'Desativar',
      danger: !ativar,
      action: async () => {
        this.substituir(await this.admin.alterarStatusUsuario(u.id, ativar));
        this.toast.success(ativar ? 'Conta reativada' : 'Conta desativada');
      }
    });
  }

  pedirRole(u: AdminUsuario): void {
    const promover = u.role !== 'ADMINISTRADOR';
    this.confirmacao.set({
      title: promover ? 'Tornar administrador' : 'Remover acesso de administrador',
      message: promover
        ? `${u.name} terá acesso total ao painel: veículos, anúncios e usuários. Conceda apenas a pessoas de confiança.`
        : `${u.name} deixará de acessar o painel imediatamente e continuará como cliente.`,
      confirmLabel: promover ? 'Tornar administrador' : 'Remover acesso',
      danger: !promover,
      action: async () => {
        this.substituir(await this.admin.alterarRoleUsuario(u.id, promover ? 'ADMINISTRADOR' : 'CLIENTE'));
        this.toast.success(promover ? 'Usuário promovido a administrador' : 'Acesso de administrador removido');
      }
    });
  }

  pedirExclusao(u: AdminUsuario): void {
    this.confirmacao.set({
      title: 'Excluir usuário',
      message: `A conta de ${u.name} (${u.email}) será removida permanentemente. Usuários com pedidos não podem ser excluídos; nesse caso, desative a conta.`,
      confirmLabel: 'Excluir',
      danger: true,
      action: async () => {
        await this.admin.excluirUsuario(u.id);
        this.todos.update((lista) => lista.filter((x) => x.id !== u.id));
        this.toast.success('Usuário excluído');
      }
    });
  }

  async confirmar(): Promise<void> {
    const req = this.confirmacao();
    if (!req) return;
    this.confirmando.set(true);
    try {
      await req.action();
    } catch (e) {
      this.toast.error(adminErrorMessage(e, 'Não foi possível concluir a ação'));
    } finally {
      this.confirmando.set(false);
      this.confirmacao.set(null);
    }
  }
}
