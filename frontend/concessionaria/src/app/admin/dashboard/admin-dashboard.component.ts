import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Dashboard } from '../../core/models/admin';
import { AdminService } from '../../core/services/admin.service';
import { formatNumber, formatPrice } from '../../core/utils/format';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private readonly admin = inject(AdminService);

  protected readonly data = signal<Dashboard | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly formatPrice = formatPrice;
  protected readonly formatNumber = formatNumber;

  /** Fatias da barra de situação dos anúncios (em %). */
  protected readonly distribuicao = computed(() => {
    const d = this.data();
    if (!d || d.totalVeiculos === 0) return [];
    return [
      { key: 'ok', label: 'Aprovados', valor: d.veiculosAprovados, status: 'APROVADO' },
      { key: 'warn', label: 'Pendentes', valor: d.veiculosPendentes, status: 'PENDENTE' },
      { key: 'off', label: 'Desativados', valor: d.veiculosDesativados, status: 'DESATIVADO' }
    ].map((f) => ({ ...f, pct: (f.valor / d.totalVeiculos) * 100 }));
  });

  async ngOnInit(): Promise<void> {
    await this.carregar();
  }

  async carregar(): Promise<void> {
    this.loading.set(true);
    this.error.set(false);
    try {
      this.data.set(await this.admin.dashboard());
    } catch {
      this.error.set(true);
    } finally {
      this.loading.set(false);
    }
  }

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime())
      ? value
      : date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
  }

  statusPedidoClass(status: string): string {
    switch (status) {
      case 'PENDENTE':
        return 'warn';
      case 'CONFIRMADO':
        return 'blue';
      case 'ENTREGUE':
        return 'ok';
      case 'CANCELADO':
        return 'off';
      default:
        return '';
    }
  }
}
