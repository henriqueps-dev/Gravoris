import { environment } from '../../../environments/environment';

/** URL base da API, definida por ambiente (src/environments). */
export const API_BASE_URL = environment.apiBaseUrl.replace(/\/+$/, '');
