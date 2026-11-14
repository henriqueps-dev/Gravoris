import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorResponse } from '../../core/models/api';

/** Mensagem legível a partir do erro devolvido pela API administrativa. */
export function adminErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) return fallback;
  if (error.status === 0) return 'Servidor indisponível no momento. Tente novamente em instantes.';
  if (error.status === 403) return 'Você não tem permissão para esta ação.';
  const body = error.error as ApiErrorResponse | string | null;
  const message = typeof body === 'string' ? body : body?.error;
  if (error.status >= 500 && error.status !== 503) return fallback;
  return message || fallback;
}
