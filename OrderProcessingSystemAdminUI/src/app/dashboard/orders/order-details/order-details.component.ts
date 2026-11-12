import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {BsModalService} from 'ngx-bootstrap/modal';
import {Observable, Subject, merge, of} from 'rxjs';
import {catchError, debounceTime, filter, map, startWith, switchMap, takeUntil, tap} from 'rxjs/operators';
import {OrderService} from '../../../services/order.service';
import {CatalogService} from '../../../services/catalog.service';
import {AnalyticsStoreService} from '../../../services/analytics-store.service';
import {AuthService} from '../../../services/auth.service';
import {AlertService} from '../../../alert/alert.service';
import {OrderResponse, OrderStatus} from '../../../services/models/order-models';
import {CustomerRef, ProductRef} from '../../../services/models/catalog-models';
import {OrderStatusChanged} from '../../../services/models/stats-models';
import {ORDER_STATUSES} from '../../../services/models/order-models';
import {nextStatuses, statusMeta} from '../../../utils/constants/order-status-constants';
import {parseApiError} from '../../../utils/error-util';
import {ChangeStatusModalComponent} from '../change-status-modal/change-status-modal.component';

/** Canonical "happy path" used to draw the progress stepper. */
const HAPPY_PATH: OrderStatus[] = ['PENDING', 'RESERVED', 'SUCCESSFUL'];

@Component({
  selector: 'app-order-details',
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.scss']
})
export class OrderDetailsComponent implements OnInit, OnDestroy {
  readonly statusMeta = statusMeta;

  orderId!: number;
  order: OrderResponse | null = null;
  loading = true;
  error: string | null = null;
  next: OrderStatus[] = [];
  steps: { status: OrderStatus; state: 'done' | 'current' | 'todo' | 'off' }[] = [];

  products$!: Observable<Map<number, ProductRef>>;
  customer$!: Observable<CustomerRef | undefined>;
  events$!: Observable<OrderStatusChanged[]>;

  private readonly reload$ = new Subject<void>();
  private readonly destroy$ = new Subject<void>();

  constructor(private route: ActivatedRoute,
              private orders: OrderService,
              private catalog: CatalogService,
              private analytics: AnalyticsStoreService,
              private modal: BsModalService,
              private alerts: AlertService,
              public auth: AuthService) {
  }

  ngOnInit(): void {
    this.products$ = this.catalog.productMap().pipe(catchError(() => of(new Map<number, ProductRef>())));

    const id$ = this.route.paramMap.pipe(map(p => Number(p.get('id'))));

    id$.pipe(
      tap(id => {
        this.orderId = id;
        this.events$ = this.analytics.eventsForOrder(id).pipe(map(list => [...list].reverse()));
      }),
      switchMap(id => merge(
        this.reload$,
        this.analytics.statusChanged$.pipe(filter(e => e.orderId === id), debounceTime(300))
      ).pipe(
        startWith(undefined),
        tap(() => (this.error = null)),
        switchMap(() => this.orders.get(id).pipe(catchError(err => {
          this.error = parseApiError(err).message;
          return of(null);
        })))
      )),
      takeUntil(this.destroy$)
    ).subscribe(order => {
      this.loading = false;
      if (order) {
        this.setOrder(order);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  refresh(): void {
    this.reload$.next();
  }

  changeStatus(target?: OrderStatus): void {
    if (!this.order) {
      return;
    }
    const ref = this.modal.show(ChangeStatusModalComponent, {
      class: 'modal-dialog-centered',
      initialState: {order: this.order, target: target ?? null}
    });
    ref.content?.changed.pipe(takeUntil(this.destroy$)).subscribe(updated => {
      this.setOrder(updated);
      this.alerts.success(`Order #${updated.id} moved to ${statusMeta(updated.status).label}.`, {title: 'Status updated'});
    });
  }

  copyId(): void {
    navigator.clipboard?.writeText(String(this.orderId)).then(() => this.alerts.info('Order ID copied.'));
  }

  private setOrder(order: OrderResponse): void {
    this.order = order;
    this.next = nextStatuses(order.status);
    this.customer$ = this.catalog.customerMap().pipe(map(m => m.get(order.customerId)), catchError(() => of(undefined)));
    this.steps = this.buildSteps(order.status);
  }

  private buildSteps(current: OrderStatus): { status: OrderStatus; state: 'done' | 'current' | 'todo' | 'off' }[] {
    if (HAPPY_PATH.includes(current)) {
      const idx = HAPPY_PATH.indexOf(current);
      return HAPPY_PATH.map((s, i) => ({status: s, state: i < idx ? 'done' : i === idx ? 'current' : 'todo'}));
    }
    // Off the happy path (RELEASED / FAILED): show where it diverged.
    const path: OrderStatus[] = current === 'RELEASED' ? ['PENDING', 'RESERVED', 'RELEASED'] : ['PENDING', 'FAILED'];
    return path.map((s, i) => ({status: s, state: i < path.length - 1 ? 'done' : 'off'}));
  }

  protected readonly allStatuses = ORDER_STATUSES;
}
