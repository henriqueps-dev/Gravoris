import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: '', renderMode: RenderMode.Prerender },
  { path: 'busca', renderMode: RenderMode.Prerender },
  { path: 'login', renderMode: RenderMode.Prerender },
  { path: 'cadastro', renderMode: RenderMode.Prerender },
  // Páginas que dependem da sessão do navegador não podem ser pré-renderizadas:
  // no build não há usuário logado e o guard gravaria o redirecionamento no HTML.
  { path: 'carrinho', renderMode: RenderMode.Client },
  { path: 'conta', renderMode: RenderMode.Client },
  // Dados do veículo vêm da API no navegador (permite hospedagem 100% estática).
  { path: 'veiculo/:id', renderMode: RenderMode.Client },
  // O painel depende da sessão do navegador: renderiza só no cliente
  { path: 'admin', renderMode: RenderMode.Client },
  { path: 'admin/**', renderMode: RenderMode.Client },
  { path: '**', renderMode: RenderMode.Prerender }
];
