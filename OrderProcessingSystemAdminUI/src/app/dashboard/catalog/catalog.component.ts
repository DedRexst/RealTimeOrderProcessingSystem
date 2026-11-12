import {Component, OnInit} from '@angular/core';
import {BsModalService} from 'ngx-bootstrap/modal';
import {forkJoin} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {CatalogService} from '../../services/catalog.service';
import {CustomerRef, ProductRef} from '../../services/models/catalog-models';
import {AuthService} from '../../services/auth.service';
import {AlertService} from '../../alert/alert.service';
import {parseApiError} from '../../utils/error-util';
import {AddProductModalComponent} from './add-product-modal/add-product-modal.component';

@Component({
  selector: 'app-catalog',
  templateUrl: './catalog.component.html'
})
export class CatalogComponent implements OnInit {
  tab: 'products' | 'customers' = 'products';
  products: ProductRef[] = [];
  customers: CustomerRef[] = [];
  search = '';
  loading = true;
  error: string | null = null;

  constructor(private catalog: CatalogService,
              private modal: BsModalService,
              private alerts: AlertService,
              public auth: AuthService) {
  }

  addProduct(): void {
    const ref = this.modal.show(AddProductModalComponent, {class: 'modal-dialog-centered', ignoreBackdropClick: true});
    ref.content?.created.subscribe(product => {
      this.alerts.success(`Product #${product.id} “${product.name}” added.`);
      this.tab = 'products';
      this.load();
    });
  }

  ngOnInit(): void {
    this.load();
  }

  load(force = false): void {
    if (force) {
      this.catalog.invalidate();
    }
    this.loading = true;
    this.error = null;
    forkJoin([this.catalog.products(), this.catalog.customers()])
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ([p, c]) => {
          this.products = p;
          this.customers = c;
        },
        error: err => (this.error = parseApiError(err).message)
      });
  }

  get filteredProducts(): ProductRef[] {
    const q = this.search.trim().toLowerCase();
    return q ? this.products.filter(p => `${p.id} ${p.name} ${p.description ?? ''}`.toLowerCase().includes(q)) : this.products;
  }

  get filteredCustomers(): CustomerRef[] {
    const q = this.search.trim().toLowerCase();
    return q ? this.customers.filter(c => `${c.id} ${c.name} ${c.address ?? ''}`.toLowerCase().includes(q)) : this.customers;
  }
}
