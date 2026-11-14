import { Component, ElementRef, OnDestroy, PLATFORM_ID, ViewChild, afterNextRender, inject, output } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.css'
})
export class LandingPage implements OnDestroy {
  readonly navigateToStore = output<void>();
  @ViewChild('bgVideo') bgVideo?: ElementRef<HTMLVideoElement>;

  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  videos: string[] = [
    'assets/videos/koenigsegg.mp4',
    'assets/videos/bugatti.mp4',
    'assets/videos/mclaren.mp4'
  ];

  currentVideoIndex = 0;
  interval: ReturnType<typeof setInterval> | undefined;

  constructor() {
    // O sorteio acontece só no navegador: assim cada visita começa por um vídeo
    // diferente (no SSR o resultado ficaria "congelado" no HTML do servidor).
    afterNextRender(() => {
      this.shuffle(this.videos);
      this.currentVideoIndex = 0;
      this.playCurrent();
      this.interval = setInterval(() => this.changeVideo(), 8000);
    });
  }

  // Fisher-Yates
  private shuffle(array: string[]): void {
    for (let i = array.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [array[i], array[j]] = [array[j], array[i]];
    }
  }

  changeVideo(): void {
    this.currentVideoIndex++;

    if (this.currentVideoIndex >= this.videos.length) {
      // Fim da rodada: embaralha de novo, evitando repetir o último vídeo no começo
      const last = this.videos[this.videos.length - 1];
      this.shuffle(this.videos);
      if (this.videos.length > 1 && this.videos[0] === last) {
        [this.videos[0], this.videos[1]] = [this.videos[1], this.videos[0]];
      }
      this.currentVideoIndex = 0;
    }

    this.playCurrent();
  }

  /** Troca o arquivo direto no elemento, sempre sem áudio. */
  private playCurrent(): void {
    if (!this.isBrowser) return;
    const el = this.bgVideo?.nativeElement;
    if (!el) return;
    el.muted = true;
    el.defaultMuted = true;
    el.volume = 0;
    el.src = this.videos[this.currentVideoIndex];
    void el.play().catch(() => {});
  }

  ngOnDestroy(): void {
    clearInterval(this.interval);
  }
}
