import {Component, Input, OnChanges} from '@angular/core';
import {StatusReasons} from '../../../services/models/stats-models';

@Component({
  selector: 'app-status-reasons',
  template: `
    <div class="card-x h-100">
      <div class="card-x__header">
        <div><h2>Top reasons</h2><span class="hint">Why orders changed status</span></div>
      </div>
      <div class="card-x__body">
        <ng-container *ngIf="groups.length; else empty">
          <div class="segmented mb-3">
            <button type="button" *ngFor="let g of groups" [class.active]="g.status === selected"
                    (click)="selected = g.status">{{ g.status | statusLabel }}</button>
          </div>
          <ng-container *ngFor="let g of groups">
            <ol class="reasons" *ngIf="g.status === selected">
              <li *ngFor="let r of g.topReasons">
                <span class="txt" [title]="r.reason">{{ r.reason }}</span>
                <span class="cnt">{{ r.count }}</span>
                <div class="bar"><div [style.width.%]="(r.count / g.topReasons[0].count) * 100" class="status-bg-{{ g.status.toLowerCase() }}"></div></div>
              </li>
            </ol>
          </ng-container>
        </ng-container>
        <ng-template #empty>
          <div class="empty-state"><i class="bi bi-chat-square-text"></i><strong>No reasons recorded</strong>
            <small>Reasons appear when a status change includes one.</small></div>
        </ng-template>
      </div>
    </div>`,
  styles: [`
    .reasons { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 12px; }
    .reasons li { display: grid; grid-template-columns: 1fr auto; gap: 4px 10px; font-size: .86rem; }
    .txt { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .cnt { font-weight: 600; font-variant-numeric: tabular-nums; }
    .bar { grid-column: 1 / -1; height: 5px; background: #f1f5f9; border-radius: 999px; overflow: hidden; }
    .bar div { height: 100%; border-radius: 999px; opacity: .8; }
  `]
})
export class StatusReasonsComponent implements OnChanges {
  @Input() reasons: Record<string, StatusReasons> = {};
  groups: StatusReasons[] = [];
  selected: string | null = null;

  ngOnChanges(): void {
    this.groups = Object.values(this.reasons)
      .filter(r => r.topReasons?.length)
      .sort((a, b) => b.totalWithReason - a.totalWithReason);
    if (!this.selected || !this.groups.some(g => g.status === this.selected)) {
      this.selected = this.groups[0]?.status ?? null;
    }
  }
}
