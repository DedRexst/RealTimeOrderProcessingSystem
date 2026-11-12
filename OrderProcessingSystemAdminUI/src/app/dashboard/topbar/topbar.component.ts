import {Component, EventEmitter, Output} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {filter, map, startWith} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {AuthService} from '../../services/auth.service';
import {LoaderService} from '../../services/loader.service';
import {StatsSocketService} from '../../services/stats-socket.service';
import {AnalyticsStoreService} from '../../services/analytics-store.service';

@Component({
  selector: 'app-topbar',
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.scss']
})
export class TopbarComponent {
  @Output() toggleSidebar = new EventEmitter<void>();

  readonly title$: Observable<string>;
  readonly loading$ = this.loader.loading$;
  readonly socketStatus$ = this.socket.status$;
  readonly lastMessageAt$ = this.analytics.select(s => s.lastMessageAt);

  constructor(public auth: AuthService,
              private loader: LoaderService,
              private socket: StatsSocketService,
              private analytics: AnalyticsStoreService,
              private router: Router,
              route: ActivatedRoute) {
    this.title$ = router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      startWith(null),
      map(() => {
        let r = route.snapshot;
        while (r.firstChild) {
          r = r.firstChild;
        }
        return (r.data['title'] as string) ?? '';
      })
    );
  }

  get initials(): string {
    const email = this.auth.email;
    return email ? email.slice(0, 2).toUpperCase() : '?';
  }

  reconnect(): void {
    this.socket.reconnectNow();
  }

  signOut(): void {
    this.auth.logout().subscribe(() => this.router.navigate(['/sign-in']));
  }
}
