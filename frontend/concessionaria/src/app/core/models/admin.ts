export type StatusAnuncio = 'PENDENTE' | 'APROVADO' | 'DESATIVADO';
export type Categoria = 'SEDAN' | 'SUV' | 'ESPORTIVO' | 'PICKUP' | 'HATCH';

export const STATUS_ANUNCIO: { value: StatusAnuncio; label: string }[] = [
  { value: 'APROVADO', label: 'Aprovado' },
  { value: 'PENDENTE', label: 'Pendente' },
  { value: 'DESATIVADO', label: 'Desativado' }
];

export const CATEGORIAS: { value: Categoria; label: string }[] = [
  { value: 'ESPORTIVO', label: 'Esportivo' },
  { value: 'SUV', label: 'SUV' },
  { value: 'SEDAN', label: 'Sedan' },
  { value: 'HATCH', label: 'Hatch' },
  { value: 'PICKUP', label: 'Pickup' }
];

export interface AdminVeiculo {
  id: number;
  name: string;
  brand: string;
  model: string;
  price: number;
  imageUrl: string | null;
  description: string | null;
  year: number;
  mileage: number;
  category: Categoria | null;
  stock: number;
  engine: string | null;
  power: number;
  torque: string | null;
  topSpeed: number;
  acceleration: string | null;
  status: StatusAnuncio;
  ativo: boolean;
  criadoEm: string | null;
  atualizadoEm: string | null;
}

export interface AdminVeiculoRequest {
  name: string;
  brand: string;
  model: string;
  price: number | null;
  imageUrl: string;
  description: string;
  year: number | null;
  mileage: number | null;
  category: Categoria;
  stock: number | null;
  engine: string;
  power: number | null;
  torque: string;
  topSpeed: number | null;
  acceleration: string;
  status: StatusAnuncio;
}

export interface AdminUsuario {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  cpf: string;
  role: 'CLIENTE' | 'ADMINISTRADOR';
  ativo: boolean;
  criadoEm: string | null;
}

export interface PedidoResumo {
  id: number;
  cliente: string;
  total: number;
  status: string;
  dataPedido: string;
  itens: number;
}

export interface Dashboard {
  totalVeiculos: number;
  veiculosAprovados: number;
  veiculosPendentes: number;
  veiculosDesativados: number;
  estoqueDisponivel: number;
  totalUsuarios: number;
  usuariosAtivos: number;
  totalAdministradores: number;
  totalPedidos: number;
  receitaPedidos: number;
  pedidosRecentes: PedidoResumo[];
}
