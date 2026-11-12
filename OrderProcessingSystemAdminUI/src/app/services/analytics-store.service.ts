import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {distinctUntilChanged, map} from 'rxjs/operators';
import {StatsSocketService} from './stats-socket.service';
import {
  CurrencyBreakdown,
  CustomerStats,
  OrderRate,
  OrderStatusChanged,
  ProductPopularity,
  StatsMessage,
  StatusFunnel,
  StatusReasons,
  TransitionTime
} from './models/stats-models';

export const MAX_RATE_WINDOWS = 60;
export const MAX_EVENTS = 200;

export interface AnalyticsState {
  orderRate: OrderRate[];
  funnel: StatusFunnel | null;
  currencies: Record<string, CurrencyBreakdown>;
  products: Record<string, ProductPopularity>;
  customers: Record<string, CustomerStats>;
  transitions: Record<string, TransitionTime>;
  reasons: Record<string, StatusReasons>;
  events: OrderStatusChanged[];
  lastMessageAt: number | null;
}

export const INITIAL_ANALYTICS_STATE: AnalyticsState = {
  orderRate: [],
  funnel: null,
  currencies: {},
  products: {},
  customers: {},
  transitions: {},
  reasons: {},
  events: [],
  lastMessageAt: null
};

export function eventKey(e: OrderStatusChanged): string {
  return `${e.orderId}|${e.newStatus}|${e.occurredAt}`;
}

export function reduceAnalytics(state: AnalyticsState, msg: StatsMessage, now = Date.now()): AnalyticsState {
  const next = {...state, lastMessageAt: now};
  switch (msg.type) {
    case 'ORDER_RATE': {
      const rate = msg.data as OrderRate;
      const rest = state.orderRate.filter(r => r.windowKey !== rate.windowKey);
      rest.push(rate);
      rest.sort((a, b) => Date.parse(a.windowKey) - Date.parse(b.windowKey));
      next.orderRate = rest.slice(-MAX_RATE_WINDOWS);
      return next;
    }
    case 'STATUS_FUNNEL':
      next.funnel = msg.data as StatusFunnel;
      return next;
    case 'CURRENCY_BREAKDOWN': {
      const d = msg.data as CurrencyBreakdown;
      next.currencies = {...state.currencies, [d.currency]: d};
      return next;
    }
    case 'PRODUCT_POPULARITY': {
      const d = msg.data as ProductPopularity;
      next.products = {...state.products, [d.productId]: d};
      return next;
    }
    case 'CUSTOMER_STATS': {
      const d = msg.data as CustomerStats;
      next.customers = {...state.customers, [d.customerId]: d};
      return next;
    }
    case 'STATUS_TRANSITION_TIME': {
      const d = msg.data as TransitionTime;
      next.transitions = {...state.transitions, [`${d.fromStatus}->${d.toStatus}`]: d};
      return next;
    }
    case 'STATUS_REASONS': {
      const d = msg.data as StatusReasons;
      next.reasons = {...state.reasons, [d.status]: d};
      return next;
    }
    case 'ORDER_STATUS_CHANGED': {
      const e = msg.data as OrderStatusChanged;
      const key = eventKey(e);
      if (state.events.some(x => eventKey(x) === key)) {
        return state;
      }
      const events = [e, ...state.events];
      events.sort((a, b) => Date.parse(b.occurredAt) - Date.parse(a.occurredAt));
      next.events = events.slice(0, MAX_EVENTS);
      return next;
    }
    default:
      return state;
  }
}

@Injectable({providedIn: 'root'})
export class AnalyticsStoreService {
  private readonly stateSubject = new BehaviorSubject<AnalyticsState>(INITIAL_ANALYTICS_STATE);
  private readonly newEventSubject = new Subject<OrderStatusChanged>();
  private started = false;

  readonly state$: Observable<AnalyticsState> = this.stateSubject.asObservable();

  readonly statusChanged$: Observable<OrderStatusChanged> = this.newEventSubject.asObservable();
  readonly status$;

  constructor(private socket: StatsSocketService) {
    this.status$ = socket.status$;
  }

  start(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    this.socket.messages$.subscribe(msg => {
      const prev = this.stateSubject.value;
      const next = reduceAnalytics(prev, msg);
      if (next === prev) {
        return;
      }
      this.stateSubject.next(next);
      if (msg.type === 'ORDER_STATUS_CHANGED') {
        this.newEventSubject.next(msg.data as OrderStatusChanged);
      }
    });
    this.socket.connect();
  }

  select<T>(project: (s: AnalyticsState) => T): Observable<T> {
    return this.state$.pipe(map(project), distinctUntilChanged());
  }

  eventsForOrder(orderId: number): Observable<OrderStatusChanged[]> {
    return this.select(s => s.events).pipe(map(events => events.filter(e => e.orderId === orderId)));
  }
}
