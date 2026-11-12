import {Component, Input, OnChanges} from '@angular/core';
import {StatusFunnel} from '../../../services/models/stats-models';
import {ORDER_STATUSES} from '../../../services/models/order-models';
import {statusMeta} from '../../../utils/constants/order-status-constants';

interface Row {
  status: string;
  label: string;
  icon: string;
  color: string;
  orders: number;
  share: number;
}

/** Funnel rendered as proportional bars (more legible than a chart for 5 categories). */
@Component({
  selector: 'app-status-funnel-chart',
  template: `
    <div class="card-x h-100">
      <div class="card-x__header">
        <div><h2>Status funnel</h2><span class="hint">Share of tracked orders that reached each status</span></div>
        <span class="chip" *ngIf="funnel">{{ funnel.totalOrders | number }} orders</span>
      </div>
      <div class="card-x__body">
        <div class="funnel" *ngIf="rows.length; else empty">
          <div class="frow" *ngFor="let r of rows">
            <div class="flabel"><i class="bi {{ r.icon }}" [style.color]="r.color"></i>{{ r.label }}</div>
            <div class="ftrack"><div class="fbar" [style.width.%]="r.share" [style.background]="r.color"></div></div>
            <div class="fval"><strong>{{ r.orders | number }}</strong><span>{{ r.share | number: '1.0-1' }}%</span></div>
          </div>
        </div>
        <ng-template #empty>
          <div class="empty-state"><i class="bi bi-funnel"></i><strong>No status changes yet</strong>
            <small>The funnel fills as orders move through the lifecycle.</small></div>
        </ng-template>
      </div>
    </div>`,
  styles: [`
    .funnel { display: flex; flex-direction: column; gap: 16px; padding-top: 4px; }
    .frow { display: grid; grid-template-columns: 120px 1fr 92px; align-items: center; gap: 14px; }
    .flabel { font-size: .86rem; font-weight: 500; display: flex; align-items: center; gap: 8px; }
    .ftrack { height: 10px; background: #f1f5f9; border-radius: 999px; overflow: hidden; }
    .fbar { height: 100%; border-radius: 999px; transition: width .6s cubic-bezier(.2,.8,.2,1); min-width: 4px; }
    .fval { text-align: right; font-size: .86rem; font-variant-numeric: tabular-nums; display: flex; justify-content: flex-end; gap: 8px; }
    .fval span { color: #94a3b8; width: 44px; }
  `]
})
export class StatusFunnelChartComponent implements OnChanges {
  @Input() funnel: StatusFunnel | null = null;
  rows: Row[] = [];

  ngOnChanges(): void {
    const stages = this.funnel?.stages ?? [];
    const order = (s: string) => {
      const i = ORDER_STATUSES.indexOf(s as never);
      return i === -1 ? 99 : i;
    };
    this.rows = [...stages]
      .sort((a, b) => order(a.status) - order(b.status))
      .map(s => {
        const m = statusMeta(s.status);
        return {status: s.status, label: m.label, icon: m.icon, color: m.color, orders: s.orders, share: s.sharePercent};
      });
  }
}
