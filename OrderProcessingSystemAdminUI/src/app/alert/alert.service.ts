import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {Alert, AlertType} from './alert.model';

@Injectable({providedIn: 'root'})
export class AlertService {
  private subject = new Subject<Alert>();
  private defaultId = 'default-alert';
  private seq = 0;

  onAlert(id = this.defaultId): Observable<Alert> {
    return this.subject.asObservable().pipe(filter(x => x && x.id === id));
  }

  success(message: string, options?: Partial<Alert>): void {
    this.alert(new Alert({...options, type: AlertType.SUCCESS, message}));
  }

  error(message: string, options?: Partial<Alert>): void {
    this.alert(new Alert({...options, type: AlertType.ERROR, message}));
  }

  info(message: string, options?: Partial<Alert>): void {
    this.alert(new Alert({...options, type: AlertType.INFO, message}));
  }

  warn(message: string, options?: Partial<Alert>): void {
    this.alert(new Alert({...options, type: AlertType.WARNING, message}));
  }

  alert(alert: Alert): void {
    alert.id = alert.id || this.defaultId;
    alert.uid = ++this.seq;
    this.subject.next(alert);
  }

  clear(id = this.defaultId): void {
    this.subject.next(new Alert({id}));
  }
}
