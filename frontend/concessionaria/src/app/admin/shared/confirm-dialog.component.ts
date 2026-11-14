import { Component, ElementRef, HostListener, effect, input, output, viewChild } from '@angular/core';

/** Pedido de confirmação exibido antes de ações destrutivas ou sensíveis. */
export interface ConfirmRequest {
  title: string;
  message: string;
  confirmLabel: string;
  danger?: boolean;
  action: () => Promise<void> | void;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    @if (request(); as req) {
      <div class="adm-dialog-backdrop" (click)="cancel.emit()">
        <div
          class="adm-dialog"
          role="alertdialog"
          aria-modal="true"
          aria-labelledby="adm-dialog-title"
          aria-describedby="adm-dialog-msg"
          (click)="$event.stopPropagation()"
        >
          <h2 id="adm-dialog-title">{{ req.title }}</h2>
          <p id="adm-dialog-msg">{{ req.message }}</p>
          <div class="adm-dialog-actions">
            <button #cancelBtn type="button" class="adm-btn" (click)="cancel.emit()" [disabled]="busy()">
              Cancelar
            </button>
            <button
              type="button"
              class="adm-btn"
              [class.danger]="req.danger"
              [class.primary]="!req.danger"
              (click)="confirm.emit()"
              [disabled]="busy()"
            >
              {{ busy() ? 'Aguarde...' : req.confirmLabel }}
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class ConfirmDialogComponent {
  readonly request = input<ConfirmRequest | null>(null);
  readonly busy = input(false);
  readonly confirm = output<void>();
  readonly cancel = output<void>();

  private readonly cancelBtn = viewChild<ElementRef<HTMLButtonElement>>('cancelBtn');

  constructor() {
    // Foco no botão seguro (Cancelar) ao abrir
    effect(() => {
      this.cancelBtn()?.nativeElement.focus();
    });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.request() && !this.busy()) this.cancel.emit();
  }
}
