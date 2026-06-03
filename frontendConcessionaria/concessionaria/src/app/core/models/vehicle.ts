export type Brand =
  | 'Bugatti'
  | 'McLaren'
  | 'Ferrari'
  | 'Lamborghini'
  | 'Koenigsegg'
  | 'Porsche'
  | 'BMW'
  | 'Mercedes-AMG'
  | 'Audi'
  | 'Rolls-Royce'
  | 'Aston Martin'
  | 'Pagani';

export const BRANDS: Brand[] = [
  'Bugatti',
  'McLaren',
  'Ferrari',
  'Lamborghini',
  'Koenigsegg',
  'Porsche',
  'BMW',
  'Mercedes-AMG',
  'Audi',
  'Rolls-Royce',
  'Aston Martin',
  'Pagani'
];

export interface Vehicle {
  id: string;
  name: string;
  brand: Brand;
  year: number;
  engine: string;
  power: number;
  torque: string;
  topSpeed: number;
  acceleration: string;
  price: number;
  image: string;
  gallery: string[];
  description: string;
}

export type SortOption = 'price-asc' | 'price-desc' | 'power-asc' | 'power-desc';

export interface VehicleFilters {
  brand: Brand | null;
  minSpeed: number | null;
  minPrice: number | null;
  maxPrice: number | null;
  sort: SortOption;
}
