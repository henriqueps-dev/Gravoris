import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: '', renderMode: RenderMode.Prerender },
  { path: 'busca', renderMode: RenderMode.Prerender },
  { path: 'login', renderMode: RenderMode.Prerender },
  { path: 'cadastro', renderMode: RenderMode.Prerender },
  { path: 'carrinho', renderMode: RenderMode.Prerender },
  { path: 'conta', renderMode: RenderMode.Prerender },
  { path: 'veiculo/:id', renderMode: RenderMode.Server },
  // O painel depende da sessão do navegador: renderiza só no cliente
  { path: 'admin', renderMode: RenderMode.Client },
  { path: 'admin/**', renderMode: RenderMode.Client },
  { path: '**', renderMode: RenderMode.Prerender }
];
