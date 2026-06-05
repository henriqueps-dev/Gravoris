import { Component, computed, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { FavoritesService } from '../../core/services/favorites.service';
import { VehicleCatalogService } from '../../core/services/vehicle-catalog.service';
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
  activeSection = 'perfil';

  protected readonly favoriteVehicles = computed(() => {
    const ids = this.favorites.favoriteIds();
    return this.catalog.getFilteredVehicles().filter((v) => ids.includes(v.id));
  });

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      const hash = window.location.hash.replace('#', '');
      if (['perfil', 'pedidos', 'favoritos', 'configuracoes'].includes(hash)) {
        this.activeSection = hash;
      }
    }
  }

  setSection(section: string): void {
    this.activeSection = section;
    if (typeof window !== 'undefined') {
      window.location.hash = section;
    }
  }
}
