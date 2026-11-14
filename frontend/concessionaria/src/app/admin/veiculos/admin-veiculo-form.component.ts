import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  AdminVeiculo,
  AdminVeiculoRequest,
  CATEGORIAS,
  STATUS_ANUNCIO
} from '../../core/models/admin';
import { AdminService } from '../../core/services/admin.service';
import { ToastService } from '../../core/services/toast.service';
import { VehicleCatalogService } from '../../core/services/vehicle-catalog.service';
import { adminErrorMessage } from '../shared/admin-error';

const FORM_VAZIO: AdminVeiculoRequest = {
  name: '',
  brand: '',
  model: '',
  price: null,
  imageUrl: '',
  description: '',
  year: new Date().getFullYear(),
  mileage: 0,
  category: 'ESPORTIVO',
  stock: 1,
  engine: '',
  power: null,
  torque: '',
  topSpeed: null,
  acceleration: '',
  status: 'APROVADO'
};

const TIPOS_ACEITOS = ['image/jpeg', 'image/png', 'image/webp', 'image/avif'];
const MAX_BYTES = 5 * 1024 * 1024;

@Component({
  selector: 'app-admin-veiculo-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-veiculo-form.component.html',
  styleUrl: './admin-veiculo-form.component.css'
})
export class AdminVeiculoFormComponent implements OnInit {
  /** Vem da rota (/admin/veiculos/:id) via withComponentInputBinding */
  readonly id = input<string>();

  private readonly admin = inject(AdminService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly catalog = inject(VehicleCatalogService);

  protected form: AdminVeiculoRequest = { ...FORM_VAZIO };
  protected readonly original = signal<AdminVeiculo | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly uploading = signal(false);
  protected readonly uploadDisponivel = signal(false);
  protected readonly imagemQuebrada = signal(false);
  protected readonly erros = signal<string[]>([]);
  protected readonly categorias = CATEGORIAS;
  protected readonly statusOpcoes = STATUS_ANUNCIO;

  get editando(): boolean {
    return !!this.id();
  }

  async ngOnInit(): Promise<void> {
    this.admin
      .uploadConfigurado()
      .then((r) => this.uploadDisponivel.set(r.configurado))
      .catch(() => this.uploadDisponivel.set(false));

    const id = Number(this.id());
    if (!this.editando) return;
    if (!Number.isInteger(id) || id <= 0) {
      void this.router.navigate(['/admin/veiculos']);
      return;
    }

    this.loading.set(true);
    try {
      const v = await this.admin.buscarVeiculo(id);
      this.original.set(v);
      this.form = {
        name: v.name,
        brand: v.brand,
        model: v.model,
        price: v.price,
        imageUrl: v.imageUrl ?? '',
        description: v.description ?? '',
        year: v.year,
        mileage: v.mileage,
        category: v.category ?? 'ESPORTIVO',
        stock: v.stock,
        engine: v.engine ?? '',
        power: v.power,
        torque: v.torque ?? '',
        topSpeed: v.topSpeed,
        acceleration: v.acceleration ?? '',
        status: v.status
      };
    } catch (e) {
      this.toast.error(adminErrorMessage(e, 'Veículo não encontrado'));
      void this.router.navigate(['/admin/veiculos']);
    } finally {
      this.loading.set(false);
    }
  }

  onImagemUrlChange(): void {
    this.imagemQuebrada.set(false);
  }

  async onArquivo(event: Event): Promise<void> {
    const inputEl = event.target as HTMLInputElement;
    const file = inputEl.files?.[0];
    inputEl.value = '';
    if (!file) return;

    if (!TIPOS_ACEITOS.includes(file.type)) {
      this.toast.error('Formato não suportado. Envie JPG, PNG, WEBP ou AVIF');
      return;
    }
    if (file.size > MAX_BYTES) {
      this.toast.error('Arquivo muito grande (máximo de 5 MB)');
      return;
    }

    this.uploading.set(true);
    try {
      const { url } = await this.admin.enviarImagem(file);
      this.form.imageUrl = url;
      this.imagemQuebrada.set(false);
      this.toast.success('Imagem enviada');
    } catch (e) {
      this.toast.error(adminErrorMessage(e, 'Não foi possível enviar a imagem'));
    } finally {
      this.uploading.set(false);
    }
  }

  private validar(): string[] {
    const f = this.form;
    const erros: string[] = [];
    if (!f.name.trim()) erros.push('Informe o nome do veículo');
    if (!f.brand.trim()) erros.push('Informe a marca');
    if (!f.model.trim()) erros.push('Informe o modelo');
    if (f.price == null || f.price <= 0) erros.push('Informe um preço maior que zero');
    if (f.year == null || f.year < 1886 || f.year > 2100) erros.push('Informe um ano válido');
    if (f.stock != null && f.stock < 0) erros.push('O estoque não pode ser negativo');
    if (f.mileage != null && f.mileage < 0) erros.push('A quilometragem não pode ser negativa');
    if (f.imageUrl.trim() && !/^https?:\/\//i.test(f.imageUrl.trim())) {
      erros.push('A URL da imagem deve começar com http:// ou https://');
    }
    return erros;
  }

  async salvar(): Promise<void> {
    if (this.saving()) return;
    const erros = this.validar();
    this.erros.set(erros);
    if (erros.length) return;

    this.saving.set(true);
    const payload: AdminVeiculoRequest = {
      ...this.form,
      name: this.form.name.trim(),
      brand: this.form.brand.trim(),
      model: this.form.model.trim(),
      imageUrl: this.form.imageUrl.trim()
    };

    try {
      const atual = this.original();
      if (atual) {
        await this.admin.atualizarVeiculo(atual.id, payload);
        this.toast.success('Veículo atualizado');
      } else {
        await this.admin.criarVeiculo(payload);
        this.toast.success(payload.status === 'APROVADO' ? 'Veículo cadastrado e publicado' : 'Veículo cadastrado');
      }
      void this.catalog.loadVitrine();
      void this.router.navigate(['/admin/veiculos']);
    } catch (e) {
      const msg = adminErrorMessage(e, 'Não foi possível salvar o veículo');
      this.erros.set(msg.split('; '));
      this.toast.error('Revise os dados do veículo');
    } finally {
      this.saving.set(false);
    }
  }
}
