import {Injectable, NgZone} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {JwtHelperService} from '@auth0/angular-jwt';
import {BehaviorSubject, Observable, of, throwError} from 'rxjs';
import {catchError, finalize, map, shareReplay, tap} from 'rxjs/operators';
import {JwtPayload, LoginRequest, ROLE_ADMIN, TokenResponse} from './models/auth-models';
import {PathConstants} from '../utils/constants/path-constant';

const ACCESS_KEY = 'opa.access_token';
const REFRESH_KEY = 'opa.refresh_token';
const EXPIRY_SKEW_SECONDS = 20;

@Injectable({providedIn: 'root'})
export class AuthService {
  private readonly jwtHelper = new JwtHelperService();
  private refreshInFlight$: Observable<string> | null = null;
  private readonly sessionSubject = new BehaviorSubject<JwtPayload | null>(null);

  readonly session$ = this.sessionSubject.asObservable();

  constructor(private http: HttpClient, zone: NgZone) {
    this.sessionSubject.next(this.decode(this.accessToken));
    // Keep tabs in sync: another tab refreshing rotates the refresh token.
    window.addEventListener('storage', (e) => {
      if (e.key === ACCESS_KEY || e.key === REFRESH_KEY || e.key === null) {
        zone.run(() => this.sessionSubject.next(this.decode(this.accessToken)));
      }
    });
  }

  get accessToken(): string | null {
    return localStorage.getItem(ACCESS_KEY);
  }

  get refreshToken(): string | null {
    return localStorage.getItem(REFRESH_KEY);
  }

  get session(): JwtPayload | null {
    return this.sessionSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.accessToken || !!this.refreshToken;
  }

  isAccessTokenExpired(): boolean {
    const token = this.accessToken;
    if (!token) {
      return true;
    }
    try {
      return this.jwtHelper.isTokenExpired(token, EXPIRY_SKEW_SECONDS);
    } catch {
      return true;
    }
  }

  isAdmin(): boolean {
    return !!this.session?.roles?.includes(ROLE_ADMIN);
  }

  get email(): string {
    return this.session?.email ?? '';
  }

  signIn(credentials: LoginRequest): Observable<JwtPayload> {
    return this.http.post<TokenResponse>(PathConstants.LOGIN, credentials).pipe(
      tap(res => this.store(res)),
      map(() => this.session as JwtPayload)
    );
  }

  /**
   * Exchanges the refresh token for a new pair. Concurrent callers share a single request:
   * the backend rotates refresh tokens and treats reuse as theft (revoking every session).
   */
  refresh(): Observable<string> {
    if (this.refreshInFlight$) {
      return this.refreshInFlight$;
    }
    const refreshToken = this.refreshToken;
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token'));
    }
    this.refreshInFlight$ = this.http.post<TokenResponse>(PathConstants.REFRESH, {refresh_token: refreshToken}).pipe(
      tap(res => this.store(res)),
      map(res => res.access_token),
      finalize(() => (this.refreshInFlight$ = null)),
      shareReplay({bufferSize: 1, refCount: false})
    );
    return this.refreshInFlight$;
  }

  /** Revokes the refresh token server-side (best effort) and clears local state. */
  logout(): Observable<void> {
    const refreshToken = this.refreshToken;
    this.clear();
    if (!refreshToken) {
      return of(void 0);
    }
    return this.http.post<void>(PathConstants.LOGOUT, {refresh_token: refreshToken}).pipe(
      catchError(() => of(void 0))
    );
  }

  clear(): void {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
    this.sessionSubject.next(null);
  }

  private store(res: TokenResponse): void {
    localStorage.setItem(ACCESS_KEY, res.access_token);
    if (res.refresh_token) {
      localStorage.setItem(REFRESH_KEY, res.refresh_token);
    }
    this.sessionSubject.next(this.decode(res.access_token));
  }

  private decode(token: string | null): JwtPayload | null {
    if (!token) {
      return null;
    }
    try {
      return this.jwtHelper.decodeToken<JwtPayload>(token);
    } catch {
      return null;
    }
  }
}
