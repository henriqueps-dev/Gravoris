import { RenderMode, ServerRoute } from '@angular/ssr';
import { VEHICLES } from './core/data/vehicles';

export const serverRoutes: ServerRoute[] = [
  { path: '', renderMode: RenderMode.Prerender },
  { path: 'busca', renderMode: RenderMode.Prerender },
  { path: 'login', renderMode: RenderMode.Prerender },
  { path: 'cadastro', renderMode: RenderMode.Prerender },
  { path: 'carrinho', renderMode: RenderMode.Prerender },
  { path: 'conta', renderMode: RenderMode.Prerender },
  {
    path: 'veiculo/:id',
    renderMode: RenderMode.Prerender,
    getPrerenderParams: async () => VEHICLES.map((vehicle) => ({ id: vehicle.id }))
  },
  { path: '**', renderMode: RenderMode.Prerender }
];
