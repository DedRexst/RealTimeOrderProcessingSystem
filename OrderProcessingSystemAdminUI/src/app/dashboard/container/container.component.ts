import {Component, HostListener, OnInit} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';
import {filter} from 'rxjs/operators';
import {AnalyticsStoreService} from '../../services/analytics-store.service';

const COLLAPSE_KEY = 'opa.sidebar-collapsed';

@Component({
  selector: 'app-container',
  templateUrl: './container.component.html'
})
export class ContainerComponent implements OnInit {
  collapsed = false;
  mobileOpen = false;

  constructor(private analytics: AnalyticsStoreService, router: Router) {
    try {
      this.collapsed = localStorage.getItem(COLLAPSE_KEY) === '1';
    } catch {
      this.collapsed = false;
    }
    router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(() => (this.mobileOpen = false));
  }

  ngOnInit(): void {
    this.analytics.start();
  }

  toggleSidebar(): void {
    if (window.innerWidth < 992) {
      this.mobileOpen = !this.mobileOpen;
      return;
    }
    this.collapsed = !this.collapsed;
    try {
      localStorage.setItem(COLLAPSE_KEY, this.collapsed ? '1' : '0');
    } catch {
      /* storage unavailable */
    }
  }

  @HostListener('document:keydown.escape')
  closeMobile(): void {
    this.mobileOpen = false;
  }
}
