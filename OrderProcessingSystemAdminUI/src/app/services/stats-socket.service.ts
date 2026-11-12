import {Injectable, NgZone, OnDestroy} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {Constants} from '../../environments/environment';
import {SocketStatus, StatsMessage} from './models/stats-models';

const MIN_BACKOFF_MS = 1000;
const MAX_BACKOFF_MS = 30000;

@Injectable({providedIn: 'root'})
export class StatsSocketService implements OnDestroy {
  private socket?: WebSocket;
  private backoff = MIN_BACKOFF_MS;
  private reconnectTimer?: ReturnType<typeof setTimeout>;
  private started = false;

  private readonly statusSubject = new BehaviorSubject<SocketStatus>('offline');
  private readonly messagesSubject = new Subject<StatsMessage>();

  readonly status$: Observable<SocketStatus> = this.statusSubject.asObservable();
  readonly messages$: Observable<StatsMessage> = this.messagesSubject.asObservable();

  constructor(private zone: NgZone) {
  }

  connect(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    this.open();
  }

  reconnectNow(): void {
    if (!this.started) {
      this.connect();
      return;
    }
    if (this.statusSubject.value === 'live') {
      return;
    }
    clearTimeout(this.reconnectTimer);
    this.backoff = MIN_BACKOFF_MS;
    this.open();
  }

  disconnect(): void {
    this.started = false;
    clearTimeout(this.reconnectTimer);
    this.socket?.close();
    this.socket = undefined;
    this.statusSubject.next('offline');
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  private open(): void {
    this.statusSubject.next('connecting');
    let ws: WebSocket;
    try {
      ws = new WebSocket(Constants.Default.ANALYTICS_WS);
    } catch {
      this.scheduleReconnect();
      return;
    }
    this.socket = ws;

    ws.onopen = () => this.zone.run(() => {
      this.backoff = MIN_BACKOFF_MS;
      this.statusSubject.next('live');
    });
    ws.onmessage = (event: MessageEvent<string>) => {
      let msg: StatsMessage;
      try {
        msg = JSON.parse(event.data) as StatsMessage;
      } catch {
        return;
      }
      if (msg && typeof msg.type === 'string') {
        this.zone.run(() => this.messagesSubject.next(msg));
      }
    };
    ws.onclose = () => this.zone.run(() => {
      if (this.socket !== ws) {
        return;
      }
      this.socket = undefined;
      if (this.started) {
        this.statusSubject.next('offline');
        this.scheduleReconnect();
      }
    });
    ws.onerror = () => ws.close();
  }

  private scheduleReconnect(): void {
    if (!this.started) {
      return;
    }
    clearTimeout(this.reconnectTimer);
    const delay = this.backoff;
    this.backoff = Math.min(this.backoff * 2, MAX_BACKOFF_MS);
    this.reconnectTimer = setTimeout(() => this.open(), delay);
  }
}
