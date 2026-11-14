import { Component, ViewEncapsulation, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css',
  // Os estilos do painel valem para todas as telas filhas
  encapsulation: ViewEncapsulation.None
})
export class AdminLayoutComponent {
  protected readonly auth = inject(AuthService);

  logout(): void {
    this.auth.logout('/admin/login');
  }
}
