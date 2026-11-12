import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {BsModalRef} from 'ngx-bootstrap/modal';
import {finalize} from 'rxjs/operators';
import {forkJoin} from 'rxjs';
import {CatalogService} from '../../../services/catalog.service';
import {OrderService} from '../../../services/order.service';
import {AlertService} from '../../../alert/alert.service';
import {CURRENCIES, Currency, OrderResponse} from '../../../services/models/order-models';
import {CustomerRef, ProductRef} from '../../../services/models/catalog-models';
import {parseApiError} from '../../../utils/error-util';

const MAX_PRODUCTS = 100;

@Component({
  selector: 'app-create-order-modal',
  templateUrl: './create-order-modal.component.html',
  styleUrls: ['./create-order-modal.component.scss']
})
export class CreateOrderModalComponent implements OnInit {
  @Output() created = new EventEmitter<OrderResponse>();

  readonly currencies = CURRENCIES;
  customers: CustomerRef[] = [];
  products: ProductRef[] = [];
  productSearch = '';
  loadingCatalog = true;
  catalogError: string | null = null;
  submitting = false;
  error: string | null = null;

  readonly form = this.fb.group({
    customerId: this.fb.control<number | null>(null, Validators.required),
    productIds: this.fb.nonNullable.control<number[]>([], [Validators.required]),
    currency: this.fb.nonNullable.control<Currency>('USD', Validators.required),
    address: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(500)])
  });

  constructor(public modalRef: BsModalRef,
              private fb: FormBuilder,
              private catalog: CatalogService,
              private orders: OrderService,
              private alerts: AlertService) {
  }

  ngOnInit(): void {
    forkJoin([this.catalog.customers(), this.catalog.products()])
      .pipe(finalize(() => (this.loadingCatalog = false)))
      .subscribe({
        next: ([customers, products]) => {
          this.customers = customers;
          this.products = products;
        },
        error: err => (this.catalogError = parseApiError(err).message)
      });
  }

  get selectedIds(): number[] {
    return this.form.controls.productIds.value;
  }

  get filteredProducts(): ProductRef[] {
    const q = this.productSearch.trim().toLowerCase();
    if (!q) {
      return this.products;
    }
    return this.products.filter(p =>
      p.name.toLowerCase().includes(q) || (p.description ?? '').toLowerCase().includes(q) || String(p.id) === q);
  }

  productById(id: number): ProductRef | undefined {
    return this.products.find(p => p.id === id);
  }

  onCustomerChange(value: string): void {
    const id = value ? Number(value) : null;
    this.form.controls.customerId.setValue(id);
    const customer = this.customers.find(c => c.id === id);
    const address = this.form.controls.address;
    // Pre-fill the address from the customer unless the user already typed a custom one.
    if (customer?.address && (!address.dirty || !address.value)) {
      address.setValue(customer.address);
    }
  }

  toggleProduct(id: number): void {
    const ids = this.selectedIds;
    const next = ids.includes(id) ? ids.filter(x => x !== id) : ids.length < MAX_PRODUCTS ? [...ids, id] : ids;
    this.form.controls.productIds.setValue(next);
    this.form.controls.productIds.markAsTouched();
  }

  setCurrency(c: Currency): void {
    this.form.controls.currency.setValue(c);
  }

  invalid(name: 'customerId' | 'productIds' | 'address'): boolean {
    const c = this.form.controls[name];
    return c.invalid && (c.dirty || c.touched);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.submitting = true;
    this.error = null;
    this.orders.create({customerId: v.customerId as number, productIds: v.productIds, currency: v.currency, address: v.address.trim()})
      .pipe(finalize(() => (this.submitting = false)))
      .subscribe({
        next: order => {
          this.alerts.success(`Order #${order.id} accepted and published to Kafka.`, {title: 'Order created'});
          this.created.emit(order);
          this.modalRef.hide();
        },
        error: err => (this.error = parseApiError(err).message)
      });
  }
}
