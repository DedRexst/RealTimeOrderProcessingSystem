import {Component, Input, OnChanges} from '@angular/core';
import {ChartConfiguration, ScriptableContext} from 'chart.js';
import {OrderRate} from '../../../services/models/stats-models';
import {CHART_COLORS} from '../../../utils/constants/chart-colors';

const DEFAULT_STEP_MS = 60_000;
const VISIBLE_WINDOWS = 30;

/** Kafka Streams only emits windows that had orders; fill the gaps with zeros up to "now". */
export function fillRateWindows(rates: OrderRate[], now: number, visible = VISIBLE_WINDOWS): { t: number; v: number }[] {
  if (!rates.length) {
    return [];
  }
  const points = rates.map(r => ({t: Date.parse(r.windowKey), v: r.orderCount}));
  let step = DEFAULT_STEP_MS;
  for (let i = 1; i < points.length; i++) {
    const d = points[i].t - points[i - 1].t;
    if (d > 0 && d < step) {
      step = d;
    }
  }
  const byT = new Map(points.map(p => [p.t, p.v]));
  const last = Math.max(points[points.length - 1].t, Math.floor(now / step) * step);
  const out: { t: number; v: number }[] = [];
  for (let t = last - (visible - 1) * step; t <= last; t += step) {
    out.push({t, v: byT.get(t) ?? 0});
  }
  return out;
}

@Component({
  selector: 'app-order-rate-chart',
  template: `
    <div class="card-x h-100">
      <div class="card-x__header">
        <div>
          <h2>Order throughput</h2>
          <span class="hint">Orders per tumbling window · last {{ visible }} windows</span>
        </div>
        <span class="chip chip-accent" *ngIf="peak">peak {{ peak }}</span>
      </div>
      <div class="card-x__body">
        <div style="height: 260px" *ngIf="hasData; else empty">
          <canvas baseChart type="line" [data]="data" [options]="options"></canvas>
        </div>
        <ng-template #empty>
          <div class="empty-state" style="height: 260px; display:flex; flex-direction:column; justify-content:center">
            <i class="bi bi-activity"></i>
            <strong>No orders streamed yet</strong>
            <small>Create an order and watch it appear here within seconds.</small>
          </div>
        </ng-template>
      </div>
    </div>`
})
export class OrderRateChartComponent implements OnChanges {
  @Input() rates: OrderRate[] = [];
  @Input() now = Date.now();

  readonly visible = VISIBLE_WINDOWS;
  hasData = false;
  peak = 0;

  data: ChartConfiguration<'line'>['data'] = {labels: [], datasets: []};
  readonly options: ChartConfiguration<'line'>['options'] = {
    interaction: {mode: 'index', intersect: false},
    plugins: {legend: {display: false}},
    scales: {
      x: {grid: {display: false}, ticks: {maxTicksLimit: 8, maxRotation: 0}},
      y: {beginAtZero: true, ticks: {precision: 0}, border: {display: false}}
    }
  };

  ngOnChanges(): void {
    const points = fillRateWindows(this.rates, this.now);
    this.hasData = points.length > 0;
    this.peak = points.reduce((m, p) => Math.max(m, p.v), 0);
    this.data = {
      labels: points.map(p => new Date(p.t).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})),
      datasets: [{
        label: 'Orders',
        data: points.map(p => p.v),
        borderColor: CHART_COLORS.accent,
        borderWidth: 2.2,
        tension: 0.35,
        fill: true,
        pointRadius: 0,
        pointHoverRadius: 5,
        pointHoverBackgroundColor: CHART_COLORS.accent,
        backgroundColor: (ctx: ScriptableContext<'line'>) => {
          const {chart} = ctx;
          const area = chart.chartArea;
          if (!area) {
            return CHART_COLORS.accentSoft;
          }
          const g = chart.ctx.createLinearGradient(0, area.top, 0, area.bottom);
          g.addColorStop(0, 'rgba(79, 70, 229, 0.28)');
          g.addColorStop(1, 'rgba(79, 70, 229, 0)');
          return g;
        }
      }]
    };
  }
}
