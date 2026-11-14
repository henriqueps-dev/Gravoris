import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-login.component.html',
  // Reaproveita o visual da tela de login da loja
  styleUrl: '../../pages/login/login.component.css'
})
export class AdminLoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  email = '';
  password = '';
  loading = signal(false);

  readonly backgroundVideo = 'assets/videos/koenigsegg.mp4';

  async submit(): Promise<void> {
    if (this.loading()) return;
    this.loading.set(true);
    const ok = await this.auth.loginAdmin(this.email, this.password);
    this.loading.set(false);

    if (ok) {
      const returnUrl = this.route.snapshot.queryParams['returnUrl'] as string | undefined;
      // Só aceita destinos internos do painel
      const destino = returnUrl && returnUrl.startsWith('/admin') ? returnUrl : '/admin';
      void this.router.navigateByUrl(destino);
    }
  }
}
