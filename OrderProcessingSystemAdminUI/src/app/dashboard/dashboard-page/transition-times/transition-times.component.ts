import {Component, Input, OnChanges} from '@angular/core';
import {TransitionTime} from '../../../services/models/stats-models';

@Component({
  selector: 'app-transition-times',
  template: `
    <div class="card-x h-100">
      <div class="card-x__header">
        <div><h2>Transition latency</h2><span class="hint">Time spent between statuses</span></div>
      </div>
      <div class="card-x__body card-x__body--flush">
        <div class="table-scroll" *ngIf="rows.length; else empty">
          <table class="table-x">
            <thead><tr><th>Transition</th><th>Average</th><th class="d-none d-md-table-cell">Distribution</th><th>Max</th><th>Last</th><th class="text-end">Samples</th></tr></thead>
            <tbody>
            <tr *ngFor="let t of rows">
              <td class="text-nowrap">
                <app-status-badge [status]="t.fromStatus"></app-status-badge>
                <i class="bi bi-arrow-right mx-1 text-muted-x"></i>
                <app-status-badge [status]="t.toStatus"></app-status-badge>
              </td>
              <td class="fw-600">{{ t.avgSeconds | duration }}</td>
              <td class="d-none d-md-table-cell" style="min-width: 140px">
                <div class="lat-track"><div class="lat-avg" [style.width.%]="pct(t.avgSeconds)"></div>
                  <div class="lat-max" [style.left.%]="pct(t.maxSeconds)"></div></div>
              </td>
              <td>{{ t.maxSeconds | duration }}</td>
              <td>{{ t.lastSeconds | duration }}</td>
              <td class="text-end">{{ t.samples | number }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <ng-template #empty>
          <div class="empty-state"><i class="bi bi-stopwatch"></i><strong>No transitions measured yet</strong></div>
        </ng-template>
      </div>
    </div>`,
  styles: [`
    .lat-track { position: relative; height: 8px; background: #f1f5f9; border-radius: 999px; }
    .lat-avg { height: 100%; border-radius: 999px; background: linear-gradient(90deg, #a5b4fc, #4f46e5); }
    .lat-max { position: absolute; top: -3px; width: 2px; height: 14px; background: #dc2626; border-radius: 2px; }
  `]
})
export class TransitionTimesComponent implements OnChanges {
  @Input() transitions: Record<string, TransitionTime> = {};
  rows: TransitionTime[] = [];
  private scale = 1;

  ngOnChanges(): void {
    this.rows = Object.values(this.transitions).sort((a, b) => b.samples - a.samples);
    this.scale = Math.max(1e-6, ...this.rows.map(r => r.maxSeconds));
  }

  pct(sec: number): number {
    return Math.min(100, (sec / this.scale) * 100);
  }
}
