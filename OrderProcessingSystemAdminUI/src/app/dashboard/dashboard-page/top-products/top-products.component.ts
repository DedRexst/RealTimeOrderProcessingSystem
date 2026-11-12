import {Component, Input, OnChanges} from '@angular/core';
import {ChartConfiguration} from 'chart.js';
import {ProductPopularity} from '../../../services/models/stats-models';
import {ProductRef} from '../../../services/models/catalog-models';
import {CHART_COLORS} from '../../../utils/constants/chart-colors';

const TOP_N = 8;

@Component({
  selector: 'app-top-products',
  template: `
    <div class="card-x h-100">
      <div class="card-x__header">
        <div><h2>Top products</h2><span class="hint">Most ordered products (all time)</span></div>
      </div>
      <div class="card-x__body">
        <div [style.height.px]="height" *ngIf="hasData; else empty">
          <canvas baseChart type="bar" [data]="data" [options]="options"></canvas>
        </div>
        <ng-template #empty>
          <div class="empty-state"><i class="bi bi-bar-chart"></i><strong>No product data yet</strong></div>
        </ng-template>
      </div>
    </div>`
})
export class TopProductsComponent implements OnChanges {
  @Input() products: Record<string, ProductPopularity> = {};
  @Input() catalog: Map<number, ProductRef> | null = null;

  hasData = false;
  height = 240;
  data: ChartConfiguration<'bar'>['data'] = {labels: [], datasets: []};
  readonly options: ChartConfiguration<'bar'>['options'] = {
    indexAxis: 'y',
    plugins: {legend: {display: false}},
    scales: {
      x: {beginAtZero: true, ticks: {precision: 0}, border: {display: false}},
      y: {grid: {display: false}, ticks: {color: '#334155', font: {weight: 500}}}
    }
  };

  ngOnChanges(): void {
    const top = Object.values(this.products).sort((a, b) => b.orderCount - a.orderCount).slice(0, TOP_N);
    this.hasData = top.length > 0;
    this.height = Math.max(160, top.length * 34 + 30);
    this.data = {
      labels: top.map(p => this.label(p.productId)),
      datasets: [{
        data: top.map(p => p.orderCount),
        backgroundColor: top.map((_, i) => i === 0 ? CHART_COLORS.accent : 'rgba(79, 70, 229, 0.35)'),
        hoverBackgroundColor: CHART_COLORS.accent,
        borderRadius: 6,
        barThickness: 18
      }]
    };
  }

  private label(id: string): string {
    const name = this.catalog?.get(Number(id))?.name;
    return name ? (name.length > 26 ? name.slice(0, 25) + '…' : name) : `#${id}`;
  }
}
