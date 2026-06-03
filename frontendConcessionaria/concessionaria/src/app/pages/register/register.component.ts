import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  fullName = '';
  email = '';
  phone = '';
  password = '';
  confirmPassword = '';
  loading = signal(false);

  readonly backgroundVideo = 'assets/videos/mclaren.mp4';

  submit(): void {
    if (this.loading()) return;
    this.loading.set(true);

    setTimeout(() => {
      const ok = this.auth.register({
        fullName: this.fullName,
        email: this.email,
        phone: this.phone,
        password: this.password,
        confirmPassword: this.confirmPassword
      });
      this.loading.set(false);

      if (ok) {
        void this.router.navigate(['/conta']);
      }
    }, 700);
  }
}
