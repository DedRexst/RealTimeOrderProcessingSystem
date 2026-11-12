import {Component, OnInit} from '@angular/core';
import {Observable, combineLatest, of, timer} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {AnalyticsState, AnalyticsStoreService} from '../../services/analytics-store.service';
import {CatalogService} from '../../services/catalog.service';
import {ProductRef} from '../../services/models/catalog-models';
import {SocketStatus} from '../../services/models/stats-models';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  state$!: Observable<AnalyticsState>;
  socketStatus$!: Observable<SocketStatus>;
  products$!: Observable<Map<number, ProductRef>>;
  /** Ticks every 15s so time-based widgets (rate chart window) roll forward without new data. */
  now$ = timer(0, 15000).pipe(map(() => Date.now()));
  vm$!: Observable<{ state: AnalyticsState; now: number }>;

  constructor(private analytics: AnalyticsStoreService, private catalog: CatalogService) {
  }

  ngOnInit(): void {
    this.state$ = this.analytics.state$;
    this.socketStatus$ = this.analytics.status$;
    this.products$ = this.catalog.productMap().pipe(catchError(() => of(new Map<number, ProductRef>())));
    this.vm$ = combineLatest([this.state$, this.now$]).pipe(map(([state, now]) => ({state, now})));
  }
}
