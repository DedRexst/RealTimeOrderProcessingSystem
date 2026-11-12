import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs';
import {Alert, AlertType} from './alert.model';
import {AlertService} from './alert.service';

const AUTO_CLOSE_MS = 5000;

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.scss']
})
export class AlertComponent implements OnInit, OnDestroy {
  @Input() id = 'default-alert';

  alerts: (Alert & {leaving?: boolean})[] = [];
  private sub?: Subscription;

  constructor(private alertService: AlertService) {
  }

  ngOnInit(): void {
    this.sub = this.alertService.onAlert(this.id).subscribe(alert => {
      if (!alert.message) {
        this.alerts = [];
        return;
      }
      this.alerts = [...this.alerts.slice(-4), alert];
      if (alert.autoClose) {
        setTimeout(() => this.remove(alert), alert.type === AlertType.ERROR ? AUTO_CLOSE_MS * 1.6 : AUTO_CLOSE_MS);
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  remove(alert: Alert): void {
    const found = this.alerts.find(a => a.uid === alert.uid);
    if (!found || found.leaving) {
      return;
    }
    found.leaving = true;
    setTimeout(() => (this.alerts = this.alerts.filter(a => a.uid !== alert.uid)), 220);
  }

  icon(alert: Alert): string {
    switch (alert.type) {
      case AlertType.SUCCESS: return 'bi-check-circle-fill';
      case AlertType.ERROR: return 'bi-exclamation-octagon-fill';
      case AlertType.WARNING: return 'bi-exclamation-triangle-fill';
      default: return 'bi-info-circle-fill';
    }
  }

  cssClass(alert: Alert): string {
    switch (alert.type) {
      case AlertType.SUCCESS: return 'toast-success';
      case AlertType.ERROR: return 'toast-error';
      case AlertType.WARNING: return 'toast-warning';
      default: return 'toast-info';
    }
  }

  trackByUid(_: number, a: Alert): number {
    return a.uid;
  }
}
