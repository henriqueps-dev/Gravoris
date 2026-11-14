import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Libera as telas do painel apenas para sessões administrativas.
 * É uma camada de conveniência: a proteção real é feita pelo backend em cada endpoint.
 */
export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  if (auth.isAdmin()) return true;
  return inject(Router).createUrlTree(['/admin/login'], { queryParams: { returnUrl: state.url } });
};

/** Quem já está numa sessão administrativa vai direto para o painel. */
export const adminGuestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (!auth.isAdmin()) return true;
  return inject(Router).createUrlTree(['/admin']);
};
