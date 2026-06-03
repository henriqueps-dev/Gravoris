import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { VehicleCatalogService } from '../../core/services/vehicle-catalog.service';
import { CartService } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';
import { AuthModalService } from '../../core/services/auth-modal.service';
import { FavoritesService } from '../../core/services/favorites.service';
import { formatPrice } from '../../core/utils/format';
import { SiteHeaderComponent } from '../../shared/components/site-header/site-header.component';
import { ScrollRevealDirective } from '../../shared/directives/scroll-reveal.directive';

@Component({
  selector: 'app-vehicle-detail',
  standalone: true,
  imports: [RouterLink, SiteHeaderComponent, ScrollRevealDirective],
  templateUrl: './vehicle-detail.component.html',
  styleUrl: './vehicle-detail.component.css'
})
export class VehicleDetailComponent {
  readonly id = input.required<string>();
  private readonly catalog = inject(VehicleCatalogService);
  protected readonly cart = inject(CartService);
  protected readonly auth = inject(AuthService);
  protected readonly favorites = inject(FavoritesService);
  private readonly authModal = inject(AuthModalService);
  protected readonly formatPrice = formatPrice;

  protected readonly vehicle = computed(() => this.catalog.getById(this.id()));

  selectedImageIndex = 0;

  addToCart(): void {
    if (!this.auth.isLoggedIn()) {
      this.authModal.open();
      return;
    }
    const v = this.vehicle();
    if (v) this.cart.add(v);
  }

  toggleFavorite(): void {
    const v = this.vehicle();
    if (v) this.favorites.toggle(v.id);
  }

  isFavorite(): boolean {
    const v = this.vehicle();
    return v ? this.favorites.isFavorite(v.id) : false;
  }

  selectImage(index: number): void {
    this.selectedImageIndex = index;
  }
}
