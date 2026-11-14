import { Component, ElementRef, OnDestroy, OnInit, output, ViewChild } from '@angular/core';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.css'
})
export class LandingPage implements OnInit, OnDestroy {
  readonly navigateToStore = output<void>();
  @ViewChild('bgVideo') bgVideo?: ElementRef<HTMLVideoElement>;

  videos: string[] = [
    'assets/videos/koenigsegg.mp4',
    'assets/videos/bugatti.mp4',
    'assets/videos/mclaren.mp4'
  ];

  currentVideoIndex: number = 0;
  currentVideo: string = this.videos[0];

  interval: any;

  ngOnInit(): void {
    this.shuffle(this.videos);
    this.currentVideoIndex = 0;
    this.currentVideo = this.videos[0];
    this.startVideoRotation();
  }

  // Fisher-Yates
  private shuffle(array: string[]): void {
    for (let i = array.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [array[i], array[j]] = [array[j], array[i]];
    }
  }

  startVideoRotation(): void {
    this.interval = setInterval(() => {
      this.changeVideo();
    }, 8000);
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

    this.currentVideo = this.videos[this.currentVideoIndex];

    queueMicrotask(() => {
      const el = this.bgVideo?.nativeElement;
      if (!el) return;
      el.load();
      void el.play().catch(() => {});
    });
  }

  ngOnDestroy(): void {
    clearInterval(this.interval);
  }
}
