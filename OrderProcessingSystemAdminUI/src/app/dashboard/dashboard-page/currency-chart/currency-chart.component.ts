import {Component, Input, OnChanges} from '@angular/core';
import {ChartConfiguration} from 'chart.js';
import {CurrencyBreakdown} from '../../../services/models/stats-models';
import {CHART_COLORS} from '../../../utils/constants/chart-colors';

@Component({
  selector: 'app-currency-chart',
  template: `
    <div class="card-x h-100">
      <div class="card-x__header">
        <div><h2>Currency mix</h2><span class="hint">Orders by currency</span></div>
      </div>
      <div class="card-x__body">
        <ng-container *ngIf="rows.length; else empty">
          <div style="height: 170px; position: relative">
            <canvas baseChart type="doughnut" [data]="data" [options]="options"></canvas>
            <div class="donut-center"><strong>{{ total | number }}</strong><span>orders</span></div>
          </div>
          <ul class="legend-list">
            <li *ngFor="let r of rows">
              <span class="sw" [style.background]="r.color"></span>
              <span class="mono fw-600">{{ r.currency }}</span>
              <span class="ms-auto">{{ r.orderCount | number }}</span>
              <span class="pct">{{ r.pct | number: '1.0-1' }}%</span>
            </li>
          </ul>
        </ng-container>
        <ng-template #empty>
          <div class="empty-state"><i class="bi bi-currency-exchange"></i><strong>No data yet</strong></div>
        </ng-template>
      </div>
    </div>`,
  styles: [`
    .donut-center { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; pointer-events: none; }
    .donut-center strong { font-size: 1.35rem; font-weight: 700; letter-spacing: -.02em; }
    .donut-center span { font-size: .72rem; color: #64748b; }
    .legend-list { list-style: none; padding: 0; margin: 18px 0 0; display: flex; flex-direction: column; gap: 8px; font-size: .86rem; }
    .legend-list li { display: flex; align-items: center; gap: 10px; }
    .sw { width: 10px; height: 10px; border-radius: 3px; }
    .pct { color: #94a3b8; width: 48px; text-align: right; font-variant-numeric: tabular-nums; }
  `]
})
export class CurrencyChartComponent implements OnChanges {
  @Input() currencies: Record<string, CurrencyBreakdown> = {};

  rows: (CurrencyBreakdown & { color: string; pct: number })[] = [];
  total = 0;
  data: ChartConfiguration<'doughnut'>['data'] = {labels: [], datasets: []};
  readonly options: ChartConfiguration<'doughnut'>['options'] = {
    cutout: '72%',
    plugins: {legend: {display: false}}
  };

  ngOnChanges(): void {
    const list = Object.values(this.currencies).sort((a, b) => b.orderCount - a.orderCount);
    this.total = list.reduce((s, c) => s + c.orderCount, 0);
    this.rows = list.map((c, i) => ({
      ...c,
      color: CHART_COLORS.currency[c.currency] ?? CHART_COLORS.series[i % CHART_COLORS.series.length],
      pct: this.total ? (c.orderCount / this.total) * 100 : 0
    }));
    this.data = {
      labels: this.rows.map(r => r.currency),
      datasets: [{
        data: this.rows.map(r => r.orderCount),
        backgroundColor: this.rows.map(r => r.color),
        borderWidth: 3,
        borderColor: '#fff',
        hoverOffset: 6
      }]
    };
  }
}
