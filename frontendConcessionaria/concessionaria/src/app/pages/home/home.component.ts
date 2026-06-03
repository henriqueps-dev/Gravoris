import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { LandingPage } from '../../landing-page/landing-page';
import { SiteHeaderComponent } from '../../shared/components/site-header/site-header.component';
import { ShowroomComponent } from '../../shared/components/showroom/showroom.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [LandingPage, SiteHeaderComponent, ShowroomComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  private readonly router = inject(Router);

  scrollToShowroom(): void {
    document.getElementById('vitrine')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  onSearch(term: string): void {
    void this.router.navigate(['/busca'], { queryParams: { q: term } });
  }
}
