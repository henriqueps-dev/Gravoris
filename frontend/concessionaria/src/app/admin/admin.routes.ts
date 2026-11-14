import { Routes } from '@angular/router';
import { adminGuard, adminGuestGuard } from '../core/guards/admin.guard';

/** Rotas do painel, carregadas sob demanda (não pesam no bundle da loja). */
export const ADMIN_ROUTES: Routes = [
  {
    path: 'login',
    canActivate: [adminGuestGuard],
    title: 'Painel · Gravoris',
    loadComponent: () => import('./login/admin-login.component').then((m) => m.AdminLoginComponent)
  },
  {
    path: '',
    canActivate: [adminGuard],
    canActivateChild: [adminGuard],
    loadComponent: () => import('./layout/admin-layout.component').then((m) => m.AdminLayoutComponent),
    children: [
      {
        path: '',
        title: 'Dashboard · Painel Gravoris',
        loadComponent: () =>
          import('./dashboard/admin-dashboard.component').then((m) => m.AdminDashboardComponent)
      },
      {
        path: 'veiculos',
        title: 'Veículos · Painel Gravoris',
        loadComponent: () =>
          import('./veiculos/admin-veiculos.component').then((m) => m.AdminVeiculosComponent)
      },
      {
        path: 'veiculos/novo',
        title: 'Novo veículo · Painel Gravoris',
        loadComponent: () =>
          import('./veiculos/admin-veiculo-form.component').then((m) => m.AdminVeiculoFormComponent)
      },
      {
        path: 'veiculos/:id',
        title: 'Editar veículo · Painel Gravoris',
        loadComponent: () =>
          import('./veiculos/admin-veiculo-form.component').then((m) => m.AdminVeiculoFormComponent)
      },
      {
        path: 'usuarios',
        title: 'Usuários · Painel Gravoris',
        loadComponent: () =>
          import('./usuarios/admin-usuarios.component').then((m) => m.AdminUsuariosComponent)
      }
    ]
  }
];
