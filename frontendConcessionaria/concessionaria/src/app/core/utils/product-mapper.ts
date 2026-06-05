import { ProdutoResponse } from '../models/api';
import { Vehicle } from '../models/vehicle';

const DEFAULT_IMAGE =
  'https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=1200&q=80';

const CATEGORY_SPECS: Record<string, { power: number; topSpeed: number; torque: string; acceleration: string }> = {
  ESPORTIVO: { power: 350, topSpeed: 250, torque: '450 Nm', acceleration: '4.5s' },
  SUV: { power: 180, topSpeed: 190, torque: '350 Nm', acceleration: '8.0s' },
  SEDAN: { power: 140, topSpeed: 200, torque: '220 Nm', acceleration: '9.0s' },
  HATCH: { power: 100, topSpeed: 170, torque: '150 Nm', acceleration: '10.5s' },
  PICKUP: { power: 200, topSpeed: 180, torque: '450 Nm', acceleration: '9.5s' }
};

export function mapProdutoToVehicle(produto: ProdutoResponse): Vehicle {
  const specs = CATEGORY_SPECS[produto.category] ?? CATEGORY_SPECS['SEDAN'];
  const image = produto.imageUrl?.startsWith('http') ? produto.imageUrl : DEFAULT_IMAGE;

  return {
    id: String(produto.id),
    name: produto.name,
    brand: produto.brand,
    model: produto.model,
    year: produto.year,
    engine: `${produto.brand} ${produto.model}`,
    power: specs.power,
    torque: specs.torque,
    topSpeed: specs.topSpeed,
    acceleration: specs.acceleration,
    price: Number(produto.price),
    image,
    gallery: [image],
    description: produto.description,
    mileage: produto.mileage,
    category: produto.category,
    stock: produto.stock
  };
}
