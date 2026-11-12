import {Component, Input} from '@angular/core';
import {AuthService} from '../../services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  link: string;
  adminOnly?: boolean;
}

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  @Input() collapsed = false;

  readonly main: NavItem[] = [
    {label: 'Dashboard', icon: 'bi-grid-1x2', link: '/dashboard'},
    {label: 'Orders', icon: 'bi-receipt', link: '/orders'},
    {label: 'Catalog', icon: 'bi-boxes', link: '/catalog'}
  ];
  readonly admin: NavItem[] = [
    {label: 'API clients', icon: 'bi-key', link: '/api-clients', adminOnly: true}
  ];

  constructor(public auth: AuthService) {
  }
}
