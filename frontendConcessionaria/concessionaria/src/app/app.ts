import { Component } from '@angular/core';
import { LandingPage } from './landing-page/landing-page';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [LandingPage],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

}
