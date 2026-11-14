// Build de produção lendo a URL da API da variável de ambiente API_URL.
// Exemplo: API_URL=https://gravoris-api.onrender.com/api npm run build:prod
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const ngBin = fileURLToPath(new URL('../node_modules/@angular/cli/bin/ng.js', import.meta.url));

const apiUrl = (process.env.API_URL || '').trim();
if (!apiUrl) {
  console.warn('[build:prod] API_URL não definida: o site vai chamar "/api" no mesmo domínio.');
} else if (!/^https:\/\//.test(apiUrl)) {
  console.error('[build:prod] API_URL deve começar com https:// em produção. Valor recebido: ' + apiUrl);
  process.exit(1);
}

const args = ['build', '--configuration', 'production'];
if (apiUrl) args.push('--define', `NG_APP_API_URL=${JSON.stringify(apiUrl)}`);

// Executa o CLI direto pelo Node (sem shell), para o valor do --define chegar com as aspas intactas
const result = spawnSync(process.execPath, [ngBin, ...args], { stdio: 'inherit' });
process.exit(result.status ?? 1);
