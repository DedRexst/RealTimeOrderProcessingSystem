import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, defer, throwError} from 'rxjs';
import {catchError, switchMap} from 'rxjs/operators';
import {AuthService} from './services/auth.service';
import {AlertService} from './alert/alert.service';
import {API_ORIGINS, PUBLIC_AUTH_PATHS} from './utils/constants/path-constant';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router,
              private auth: AuthService,
              private alertService: AlertService) {
  }

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!this.isApiCall(req.url) || PUBLIC_AUTH_PATHS.some(p => req.url.startsWith(p))) {
      return next.handle(req);
    }

    const send = () => next.handle(this.withToken(req));

    const first$ = (this.auth.isAccessTokenExpired() && this.auth.refreshToken)
      ? this.auth.refresh().pipe(
        catchError(err => this.endSession(err)),
        switchMap(() => send()))
      : defer(send);

    let retried = false;
    return first$.pipe(
      catchError((err: unknown) => {
        if (!(err instanceof HttpErrorResponse)) {
          return throwError(() => err);
        }
        if (err.status === 401 && !retried && this.auth.refreshToken) {
          retried = true;
          return this.auth.refresh().pipe(
            catchError(refreshErr => this.endSession(refreshErr)),
            switchMap(() => send()),
            catchError((retryErr: unknown) =>
              retryErr instanceof HttpErrorResponse && retryErr.status === 401
                ? this.endSession(retryErr)
                : throwError(() => retryErr))
          );
        }
        if (err.status === 401) {
          return this.endSession(err);
        }
        if (err.status === 403) {
          this.alertService.warn('You do not have permission to perform this action.', {title: 'Access denied'});
        }
        return throwError(() => err);
      })
    );
  }

  private withToken(req: HttpRequest<unknown>): HttpRequest<unknown> {
    const token = this.auth.accessToken;
    return token ? req.clone({setHeaders: {Authorization: `Bearer ${token}`}}) : req;
  }

  private endSession(err: unknown): Observable<never> {
    if (this.auth.isLoggedIn()) {
      this.auth.clear();
      this.alertService.info('Your session has ended. Please sign in again.');
    }
    this.router.navigate(['/sign-in'], {queryParams: {returnUrl: this.router.url}});
    return throwError(() => err);
  }

  private isApiCall(url: string): boolean {
    return API_ORIGINS.some(origin => url.startsWith(origin));
  }
}
