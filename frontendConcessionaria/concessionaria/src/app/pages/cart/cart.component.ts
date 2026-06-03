import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../core/services/cart.service';
import { ToastService } from '../../core/services/toast.service';
import { formatPrice } from '../../core/utils/format';
import { SiteHeaderComponent } from '../../shared/components/site-header/site-header.component';
import { ScrollRevealDirective } from '../../shared/directives/scroll-reveal.directive';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [RouterLink, FormsModule, SiteHeaderComponent, ScrollRevealDirective],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent {
  protected readonly cart = inject(CartService);
  private readonly toast = inject(ToastService);
  protected readonly formatPrice = formatPrice;

  updateQty(id: string, qty: number): void {
    this.cart.updateQuantity(id, qty);
  }

  remove(id: string): void {
    this.cart.remove(id);
  }

  clear(): void {
    this.cart.clear();
  }

  checkout(): void {
    if (this.cart.cartItems().length === 0) return;
    this.cart.clear(false);
    this.toast.success('Compra realizada com sucesso');
  }
}
