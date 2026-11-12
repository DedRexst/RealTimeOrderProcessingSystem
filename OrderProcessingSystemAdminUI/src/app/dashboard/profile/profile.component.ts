import {Component, OnInit} from '@angular/core';
import {finalize} from 'rxjs/operators';
import {UserService} from '../../services/user.service';
import {AuthService} from '../../services/auth.service';
import {UserDto} from '../../services/models/auth-models';
import {parseApiError} from '../../utils/error-util';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  user: UserDto | null = null;
  loading = true;
  error: string | null = null;

  constructor(private users: UserService, public auth: AuthService) {
  }

  ngOnInit(): void {
    this.users.me().pipe(finalize(() => (this.loading = false))).subscribe({
      next: u => (this.user = u),
      error: err => (this.error = parseApiError(err).message)
    });
  }

  get tokenExpiry(): Date | null {
    const exp = this.auth.session?.exp;
    return exp ? new Date(exp * 1000) : null;
  }
}
