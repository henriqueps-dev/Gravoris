import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toast = inject(ToastService);

  email = '';
  password = '';
  remember = false;
  loading = signal(false);

  readonly backgroundVideo = 'assets/videos/bugatti.mp4';

  async submit(): Promise<void> {
    if (this.loading()) return;
    this.loading.set(true);

    const ok = await this.auth.login(this.email, this.password, this.remember);
    this.loading.set(false);

    if (ok) {
      const returnUrl = this.route.snapshot.queryParams['returnUrl'] as string;
      void this.router.navigateByUrl(returnUrl || '/');
    }
  }

  forgotPassword(): void {
    this.toast.info('Em breve: recuperação de senha por e-mail.');
  }
}
