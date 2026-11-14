import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminVeiculo, StatusAnuncio } from '../../core/models/admin';
import { AdminService } from '../../core/services/admin.service';
import { ToastService } from '../../core/services/toast.service';
import { VehicleCatalogService } from '../../core/services/vehicle-catalog.service';
import { adminErrorMessage } from '../shared/admin-error';
import { ConfirmDialogComponent, ConfirmRequest } from '../shared/confirm-dialog.component';
import { formatPrice } from '../../core/utils/format';

type Filtro = '' | StatusAnuncio;

@Component({
  selector: 'app-admin-veiculos',
  standalone: true,
  imports: [FormsModule, RouterLink, ConfirmDialogComponent],
  templateUrl: './admin-veiculos.component.html',
  styleUrl: './admin-veiculos.component.css'
})
export class AdminVeiculosComponent implements OnInit {
  private readonly admin = inject(AdminService);
  private readonly toast = inject(ToastService);
  private readonly catalog = inject(VehicleCatalogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly todos = signal<AdminVeiculo[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly filtro = signal<Filtro>('');
  protected readonly busyId = signal<number | null>(null);
  protected readonly confirmacao = signal<ConfirmRequest | null>(null);
  protected readonly confirmando = signal(false);
  protected readonly formatPrice = formatPrice;
  protected busca = '';

  protected readonly filtros: { value: Filtro; label: string }[] = [
    { value: '', label: 'Todos' },
    { value: 'APROVADO', label: 'Aprovados' },
    { value: 'PENDENTE', label: 'Pendentes' },
    { value: 'DESATIVADO', label: 'Desativados' }
  ];

  protected readonly contagem = computed(() => {
    const lista = this.todos();
    return {
      '': lista.length,
      APROVADO: lista.filter((v) => v.status === 'APROVADO').length,
      PENDENTE: lista.filter((v) => v.status === 'PENDENTE').length,
      DESATIVADO: lista.filter((v) => v.status === 'DESATIVADO').length
    } as Record<Filtro, number>;
  });

  protected readonly visiveis = computed(() => {
    const f = this.filtro();
    return f ? this.todos().filter((v) => v.status === f) : this.todos();
  });

  async ngOnInit(): Promise<void> {
    const status = this.route.snapshot.queryParamMap.get('status') as Filtro | null;
    if (status && ['APROVADO', 'PENDENTE', 'DESATIVADO'].includes(status)) {
      this.filtro.set(status);
    }
    await this.carregar();
  }

  async carregar(): Promise<void> {
    this.loading.set(true);
    this.error.set(false);
    try {
      this.todos.set(await this.admin.listarVeiculos(this.busca));
    } catch {
      this.error.set(true);
    } finally {
      this.loading.set(false);
    }
  }

  setFiltro(f: Filtro): void {
    this.filtro.set(f);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { status: f || null },
      replaceUrl: true
    });
  }

  statusClass(status: StatusAnuncio): string {
    return status === 'APROVADO' ? 'ok' : status === 'PENDENTE' ? 'warn' : 'off';
  }

  statusLabel(status: StatusAnuncio): string {
    return status === 'APROVADO' ? 'Aprovado' : status === 'PENDENTE' ? 'Pendente' : 'Desativado';
  }

  async alterarStatus(v: AdminVeiculo, status: StatusAnuncio): Promise<void> {
    if (this.busyId()) return;
    this.busyId.set(v.id);
    try {
      const atualizado = await this.admin.alterarStatusVeiculo(v.id, status);
      this.todos.update((lista) => lista.map((x) => (x.id === v.id ? atualizado : x)));
      const msg = status === 'APROVADO' ? 'Anúncio aprovado e publicado' : status === 'DESATIVADO' ? 'Anúncio desativado' : 'Anúncio marcado como pendente';
      this.toast.success(msg);
      void this.catalog.loadVitrine();
    } catch (e) {
      this.toast.error(adminErrorMessage(e, 'Não foi possível alterar o anúncio'));
    } finally {
      this.busyId.set(null);
    }
  }

  pedirExclusao(v: AdminVeiculo): void {
    this.confirmacao.set({
      title: 'Excluir veículo',
      message: `"${v.brand} ${v.name}" será removido permanentemente. Se ele já tiver pedidos, a exclusão será bloqueada e você poderá apenas desativar o anúncio.`,
      confirmLabel: 'Excluir',
      danger: true,
      action: async () => {
        await this.admin.excluirVeiculo(v.id);
        this.todos.update((lista) => lista.filter((x) => x.id !== v.id));
        this.toast.success('Veículo excluído');
        void this.catalog.loadVitrine();
      }
    });
  }

  async confirmar(): Promise<void> {
    const req = this.confirmacao();
    if (!req) return;
    this.confirmando.set(true);
    try {
      await req.action();
      this.confirmacao.set(null);
    } catch (e) {
      this.toast.error(adminErrorMessage(e, 'Não foi possível concluir a ação'));
      this.confirmacao.set(null);
    } finally {
      this.confirmando.set(false);
    }
  }
}
