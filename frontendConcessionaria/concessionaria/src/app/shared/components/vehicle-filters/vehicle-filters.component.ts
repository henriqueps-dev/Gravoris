import { Component, inject, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SortOption } from '../../../core/models/vehicle';
import { VehicleCatalogService } from '../../../core/services/vehicle-catalog.service';
import { ScrollRevealDirective } from '../../directives/scroll-reveal.directive';

@Component({
  selector: 'app-vehicle-filters',
  standalone: true,
  imports: [FormsModule, ScrollRevealDirective],
  templateUrl: './vehicle-filters.component.html',
  styleUrl: './vehicle-filters.component.css'
})
export class VehicleFiltersComponent {
  readonly filtersChanged = output<void>();
  protected readonly catalog = inject(VehicleCatalogService);
  protected readonly sortOptions: { value: SortOption; label: string }[] = [
    { value: 'price-desc', label: 'Preço: maior → menor' },
    { value: 'price-asc', label: 'Preço: menor → maior' },
    { value: 'power-desc', label: 'Potência: maior → menor' },
    { value: 'power-asc', label: 'Potência: menor → maior' }
  ];

  speedOptions = [
    { value: null, label: 'Qualquer velocidade' },
    { value: 150, label: '150+ km/h' },
    { value: 180, label: '180+ km/h' },
    { value: 200, label: '200+ km/h' }
  ];

  priceRanges = [
    { min: null, max: null, label: 'Qualquer preço' },
    { min: null, max: 100000, label: 'Até R$ 100 mil' },
    { min: 100000, max: 200000, label: 'R$ 100 mil – R$ 200 mil' },
    { min: 200000, max: 300000, label: 'R$ 200 mil – R$ 300 mil' },
    { min: 300000, max: null, label: 'Acima de R$ 300 mil' }
  ];

  onSortChange(value: SortOption): void {
    this.catalog.updateFilters({ sort: value });
    this.filtersChanged.emit();
  }

  onSpeedChange(value: number | null): void {
    this.catalog.updateFilters({ minSpeed: value });
    this.filtersChanged.emit();
  }

  onBrandFilterChange(value: string): void {
    const brand = value || null;
    this.catalog.setBrand(brand);
    this.filtersChanged.emit();
  }

  onPriceRangeChange(index: number): void {
    const range = this.priceRanges[index];
    this.catalog.updateFilters({ minPrice: range.min, maxPrice: range.max });
    this.filtersChanged.emit();
  }

  get selectedPriceIndex(): number {
    const { minPrice, maxPrice } = this.catalog.filters();
    return this.priceRanges.findIndex((r) => r.min === minPrice && r.max === maxPrice);
  }
}
