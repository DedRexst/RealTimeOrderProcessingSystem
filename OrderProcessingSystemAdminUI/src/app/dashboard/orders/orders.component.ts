import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {BsModalService} from 'ngx-bootstrap/modal';
import {BehaviorSubject, Observable, Subject, combineLatest, merge, of} from 'rxjs';
import {catchError, debounceTime, filter, map, switchMap, takeUntil, tap} from 'rxjs/operators';
import {OrderService} from '../../services/order.service';
import {CatalogService} from '../../services/catalog.service';
import {AnalyticsStoreService} from '../../services/analytics-store.service';
import {AuthService} from '../../services/auth.service';
import {ORDER_STATUSES, OrderQuery, OrderStatus, OrderSummary} from '../../services/models/order-models';
import {PageResponse} from '../../services/models/page-models';
import {CustomerRef, ProductRef} from '../../services/models/catalog-models';
import {parseApiError} from '../../utils/error-util';
import {statusMeta} from '../../utils/constants/order-status-constants';
import {CreateOrderModalComponent} from './create-order-modal/create-order-modal.component';

export const PAGE_SIZES = [10, 20, 50];
const DEFAULT_SIZE = 20;

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit, OnDestroy {
  readonly statuses = ORDER_STATUSES;
  readonly pageSizes = PAGE_SIZES;
  readonly statusMeta = statusMeta;

  query: OrderQuery = {page: 0, size: DEFAULT_SIZE, status: null, customerId: null};
  page: PageResponse<OrderSummary> | null = null;
  loading = true;
  error: string | null = null;
  flashed = new Set<number>();

  products$!: Observable<Map<number, ProductRef>>;
  customers$!: Observable<CustomerRef[]>;

  private readonly reload$ = new BehaviorSubject<void>(undefined);
  private readonly destroy$ = new Subject<void>();

  constructor(private orders: OrderService,
              private catalog: CatalogService,
              private analytics: AnalyticsStoreService,
              private modal: BsModalService,
              public auth: AuthService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.products$ = this.catalog.productMap().pipe(catchError(() => of(new Map<number, ProductRef>())));
    this.customers$ = this.catalog.customers().pipe(catchError(() => of([])));

    // Live refresh: re-query when an event touches a visible order, or a newer order appears on page 1.
    const live$ = this.analytics.statusChanged$.pipe(
      filter(e => !!this.page && (
        this.page.content.some(o => o.id === e.orderId) ||
        (this.query.page === 0 && e.orderId > (this.page.content[0]?.id ?? 0)))),
      tap(e => this.flash(e.orderId)),
      debounceTime(600)
    );

    combineLatest([this.route.queryParamMap.pipe(map(p => this.parse(p))), merge(this.reload$, live$)])
      .pipe(
        tap(([q]) => {
          this.query = q;
          this.error = null;
          if (!this.page) {
            this.loading = true;
          }
        }),
        switchMap(([q]) => this.orders.list(q).pipe(
          catchError(err => {
            this.error = parseApiError(err).message;
            return of(null);
          }))),
        takeUntil(this.destroy$)
      )
      .subscribe(page => {
        this.loading = false;
        if (page) {
          this.page = page;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setStatus(status: OrderStatus | null): void {
    this.navigate({status, page: 0});
  }

  setCustomer(value: string): void {
    this.navigate({customerId: value ? Number(value) : null, page: 0});
  }

  setSize(value: string): void {
    this.navigate({size: Number(value), page: 0});
  }

  goTo(page: number): void {
    if (!this.page || page < 0 || page >= this.page.totalPages) {
      return;
    }
    this.navigate({page});
  }

  refresh(): void {
    this.reload$.next();
  }

  clearFilters(): void {
    this.navigate({status: null, customerId: null, page: 0});
  }

  open(order: OrderSummary): void {
    this.router.navigate(['/orders', order.id]);
  }

  openCreate(): void {
    const ref = this.modal.show(CreateOrderModalComponent, {class: 'modal-lg modal-dialog-centered', ignoreBackdropClick: true});
    ref.content?.created.pipe(takeUntil(this.destroy$)).subscribe(order => {
      this.router.navigate(['/orders', order.id]);
    });
  }

  get pages(): (number | null)[] {
    if (!this.page) {
      return [];
    }
    const total = this.page.totalPages;
    const cur = this.query.page;
    const set = new Set<number>([0, total - 1, cur - 1, cur, cur + 1].filter(p => p >= 0 && p < total));
    const sorted = [...set].sort((a, b) => a - b);
    const out: (number | null)[] = [];
    sorted.forEach((p, i) => {
      if (i > 0 && p - sorted[i - 1] > 1) {
        out.push(null);
      }
      out.push(p);
    });
    return out;
  }

  get rangeLabel(): string {
    if (!this.page || !this.page.totalElements) {
      return '0 results';
    }
    const from = this.page.page * this.page.size + 1;
    const to = Math.min(from + this.page.content.length - 1, this.page.totalElements);
    return `${from}–${to} of ${this.page.totalElements}`;
  }

  get hasFilters(): boolean {
    return !!this.query.status || this.query.customerId != null;
  }

  trackById(_: number, o: OrderSummary): number {
    return o.id;
  }

  private flash(id: number): void {
    this.flashed.add(id);
    setTimeout(() => this.flashed.delete(id), 1600);
  }

  private navigate(patch: Partial<OrderQuery>): void {
    const q = {...this.query, ...patch};
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        status: q.status || null,
        customerId: q.customerId ?? null,
        page: q.page || null,
        size: q.size !== DEFAULT_SIZE ? q.size : null
      }
    });
  }

  private parse(p: ParamMap): OrderQuery {
    const status = p.get('status') as OrderStatus | null;
    const customer = p.get('customerId');
    const size = Number(p.get('size'));
    return {
      status: status && ORDER_STATUSES.includes(status) ? status : null,
      customerId: customer && !Number.isNaN(Number(customer)) ? Number(customer) : null,
      page: Math.max(0, Number(p.get('page')) || 0),
      size: PAGE_SIZES.includes(size) ? size : DEFAULT_SIZE
    };
  }
}
