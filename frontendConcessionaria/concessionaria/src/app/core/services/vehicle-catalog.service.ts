import { Injectable, signal } from '@angular/core';
import { VEHICLES } from '../data/vehicles';
import { Brand, SortOption, Vehicle, VehicleFilters } from '../models/vehicle';

const DEFAULT_FILTERS: VehicleFilters = {
  brand: null,
  minSpeed: null,
  minPrice: null,
  maxPrice: null,
  sort: 'price-desc'
};

@Injectable({ providedIn: 'root' })
export class VehicleCatalogService {
  private readonly allVehicles = VEHICLES;
  readonly filters = signal<VehicleFilters>({ ...DEFAULT_FILTERS });
  readonly searchQuery = signal('');

  getById(id: string): Vehicle | undefined {
    return this.allVehicles.find((v) => v.id === id);
  }

  setBrand(brand: Brand | null): void {
    this.filters.update((f) => ({ ...f, brand }));
  }

  updateFilters(partial: Partial<VehicleFilters>): void {
    this.filters.update((f) => ({ ...f, ...partial }));
  }

  resetFilters(): void {
    this.filters.set({ ...DEFAULT_FILTERS });
    this.searchQuery.set('');
  }

  getFilteredVehicles(): Vehicle[] {
    const { brand, minSpeed, minPrice, maxPrice, sort } = this.filters();
    const query = this.searchQuery().trim().toLowerCase();

    let result = [...this.allVehicles];

    if (query) {
      result = result.filter(
        (v) =>
          v.name.toLowerCase().includes(query) ||
          v.brand.toLowerCase().includes(query) ||
          v.engine.toLowerCase().includes(query)
      );
    }

    if (brand) {
      result = result.filter((v) => v.brand === brand);
    }

    if (minSpeed != null) {
      result = result.filter((v) => v.topSpeed >= minSpeed);
    }

    if (minPrice != null) {
      result = result.filter((v) => v.price >= minPrice);
    }

    if (maxPrice != null) {
      result = result.filter((v) => v.price <= maxPrice);
    }

    return this.sortVehicles(result, sort);
  }

  search(term: string): Vehicle[] {
    this.searchQuery.set(term);
    return this.getFilteredVehicles();
  }

  private sortVehicles(vehicles: Vehicle[], sort: SortOption): Vehicle[] {
    const sorted = [...vehicles];
    switch (sort) {
      case 'price-asc':
        return sorted.sort((a, b) => a.price - b.price);
      case 'price-desc':
        return sorted.sort((a, b) => b.price - a.price);
      case 'power-asc':
        return sorted.sort((a, b) => a.power - b.power);
      case 'power-desc':
        return sorted.sort((a, b) => b.power - a.power);
      default:
        return sorted;
    }
  }
}
