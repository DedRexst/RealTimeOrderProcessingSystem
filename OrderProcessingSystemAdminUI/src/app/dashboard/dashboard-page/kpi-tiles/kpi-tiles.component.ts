import {Component, Input, OnChanges} from '@angular/core';
import {AnalyticsState} from '../../../services/analytics-store.service';

interface Tile {
  label: string;
  value: string;
  hint: string;
  icon: string;
  tone: 'indigo' | 'sky' | 'green' | 'red' | 'amber' | 'violet';
  spark?: string;
  delta?: { text: string; up: boolean } | null;
}

@Component({
  selector: 'app-kpi-tiles',
  templateUrl: './kpi-tiles.component.html',
  styleUrls: ['./kpi-tiles.component.scss']
})
export class KpiTilesComponent implements OnChanges {
  @Input({required: true}) state!: AnalyticsState;
  @Input() now = Date.now();

  tiles: Tile[] = [];

  ngOnChanges(): void {
    const s = this.state;
    const totalCreated = Object.values(s.currencies).reduce((sum, c) => sum + c.orderCount, 0);
    const share = (status: string) => s.funnel?.stages.find(x => x.status === status)?.sharePercent;
    const success = share('SUCCESSFUL');
    const failed = share('FAILED');
    const reserve = s.transitions['PENDING->RESERVED'];
    const rates = s.orderRate;
    const last = rates.at(-1);
    const prev = rates.at(-2);
    const fmt = new Intl.NumberFormat('en-US');

    this.tiles = [
      {
        label: 'Orders created', icon: 'bi-receipt', tone: 'indigo',
        value: totalCreated ? fmt.format(totalCreated) : '—',
        hint: `${Object.keys(s.customers).length} customers`,
        spark: this.sparkline(rates.slice(-20).map(r => r.orderCount))
      },
      {
        label: 'Latest window', icon: 'bi-lightning-charge', tone: 'sky',
        value: last ? fmt.format(last.orderCount) : '—',
        hint: last ? `window @ ${new Date(last.windowKey).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})}` : 'no orders yet',
        delta: last && prev ? this.delta(last.orderCount, prev.orderCount) : null
      },
      {
        label: 'Success rate', icon: 'bi-check2-circle', tone: 'green',
        value: success != null ? `${success.toFixed(1)}%` : '—',
        hint: s.funnel ? `of ${fmt.format(s.funnel.totalOrders)} tracked orders` : 'awaiting status events'
      },
      {
        label: 'Failure rate', icon: 'bi-x-octagon', tone: 'red',
        value: failed != null ? `${failed.toFixed(1)}%` : '—',
        hint: s.reasons['FAILED'] ? `${s.reasons['FAILED'].totalWithReason} with a reason` : 'no failures recorded'
      },
      {
        label: 'Avg. time to reserve', icon: 'bi-stopwatch', tone: 'amber',
        value: reserve ? this.duration(reserve.avgSeconds) : '—',
        hint: reserve ? `PENDING → RESERVED · ${reserve.samples} samples` : 'no samples yet'
      }
    ];
  }

  private delta(curr: number, prev: number): { text: string; up: boolean } {
    const diff = curr - prev;
    return {text: `${diff >= 0 ? '+' : ''}${diff} vs prev`, up: diff >= 0};
  }

  private duration(sec: number): string {
    if (sec < 1) {
      return `${Math.round(sec * 1000)}ms`;
    }
    if (sec < 60) {
      return `${sec.toFixed(1)}s`;
    }
    return `${Math.floor(sec / 60)}m ${Math.round(sec % 60)}s`;
  }

  /** Builds an SVG polyline path for a 100x28 viewBox. */
  private sparkline(values: number[]): string | undefined {
    if (values.length < 2) {
      return undefined;
    }
    const max = Math.max(...values, 1);
    const step = 100 / (values.length - 1);
    return values.map((v, i) => `${i === 0 ? 'M' : 'L'}${(i * step).toFixed(1)},${(26 - (v / max) * 24).toFixed(1)}`).join(' ');
  }

  trackByLabel(_: number, t: Tile): string {
    return t.label;
  }
}
