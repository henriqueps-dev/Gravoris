import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { FavoritesService } from '../../core/services/favorites.service';
import { VehicleCatalogService } from '../../core/services/vehicle-catalog.service';
import { OrderService } from '../../core/services/order.service';
import { PedidoResponse } from '../../core/models/api';
import { formatPrice } from '../../core/utils/format';
import { SiteHeaderComponent } from '../../shared/components/site-header/site-header.component';
import { VehicleCardComponent } from '../../shared/components/vehicle-card/vehicle-card.component';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [RouterLink, SiteHeaderComponent, VehicleCardComponent],
  templateUrl: './account.component.html',
  styleUrl: './account.component.css'
})
export class AccountComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  protected readonly favorites = inject(FavoritesService);
  protected readonly catalog = inject(VehicleCatalogService);
  private readonly orderService = inject(OrderService);

  activeSection = 'perfil';
  orders = signal<PedidoResponse[]>([]);
  loadingOrders = signal(false);

  protected readonly formatPrice = formatPrice;

  protected readonly favoriteVehicles = computed(() => {
    const ids = this.favorites.favoriteIds();
    return this.catalog.getFilteredVehicles().filter((v) => ids.includes(v.id));
  });

  async ngOnInit(): Promise<void> {
    if (typeof window !== 'undefined') {
      const hash = window.location.hash.replace('#', '');
      if (['perfil', 'pedidos', 'favoritos', 'configuracoes'].includes(hash)) {
        this.activeSection = hash;
      }
    }
    await this.loadOrders();
  }

  async setSection(section: string): Promise<void> {
    this.activeSection = section;
    if (typeof window !== 'undefined') {
      window.location.hash = section;
    }
    if (section === 'pedidos') {
      await this.loadOrders();
    }
  }

  async loadOrders(): Promise<void> {
    const clienteId = this.auth.getClienteId();
    if (!clienteId) return;

    this.loadingOrders.set(true);
    try {
      const result = await this.orderService.getOrdersByCliente(clienteId);
      // Ordenar por ID decrescente (mais recentes primeiro)
      result.sort((a, b) => b.id - a.id);
      this.orders.set(result);
    } catch (error) {
      console.error('Erro ao carregar pedidos:', error);
    } finally {
      this.loadingOrders.set(false);
    }
  }

  formatDate(dateStr: string): string {
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateStr;
    }
  }

  getStatusClass(status: string): string {
    const s = status.toLowerCase();
    if (s.includes('pendente')) return 'status-pending';
    if (s.includes('pago') || s.includes('finalizado') || s.includes('entregue')) return 'status-success';
    if (s.includes('cancelado')) return 'status-canceled';
    return '';
  }
}
