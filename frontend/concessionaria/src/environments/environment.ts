/**
 * Ambiente de PRODUÇÃO (padrão do `ng build`).
 *
 * A URL da API é definida no momento do build, sem precisar editar código:
 *   ng build --define "NG_APP_API_URL='https://sua-api.onrender.com/api'"
 * (o script `npm run build:prod` já faz isso lendo a variável de ambiente API_URL).
 * Se nada for definido, usa "/api" no mesmo domínio do site.
 */
declare const NG_APP_API_URL: string | undefined;

export const environment = {
  production: true,
  apiBaseUrl: typeof NG_APP_API_URL !== 'undefined' && NG_APP_API_URL ? NG_APP_API_URL : '/api'
};
