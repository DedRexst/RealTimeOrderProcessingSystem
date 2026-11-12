import {TestBed} from '@angular/core/testing';
import {HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {Router} from '@angular/router';
import {AuthInterceptor} from './authInterceptor';
import {AuthService} from './services/auth.service';
import {PathConstants} from './utils/constants/path-constant';

function jwt(exp: number): string {
  const b64 = (o: object) => btoa(JSON.stringify(o)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_');
  return `${b64({alg: 'none'})}.${b64({sub: '1', email: 'a@b.c', roles: ['ADMIN'], exp, iat: exp - 900})}.sig`;
}

describe('AuthInterceptor', () => {
  let http: HttpClient;
  let ctrl: HttpTestingController;
  let router: jasmine.SpyObj<Router>;
  const future = () => Math.floor(Date.now() / 1000) + 600;

  beforeEach(() => {
    localStorage.clear();
    router = jasmine.createSpyObj<Router>('Router', ['navigate'], {url: '/orders'});
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
        {provide: Router, useValue: router}
      ]
    });
    http = TestBed.inject(HttpClient);
    ctrl = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    ctrl.verify();
    localStorage.clear();
  });

  function login(access: string): void {
    localStorage.setItem('opa.access_token', access);
    localStorage.setItem('opa.refresh_token', 'r1');
  }

  it('adds the bearer token to API calls but not to third-party URLs', () => {
    const token = jwt(future());
    login(token);
    http.get(PathConstants.ORDERS).subscribe();
    http.get('https://example.org/x').subscribe();
    expect(ctrl.expectOne(PathConstants.ORDERS).request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    expect(ctrl.expectOne('https://example.org/x').request.headers.has('Authorization')).toBeFalse();
  });

  it('refreshes once on 401 and retries the request', () => {
    login(jwt(future()));
    let result: unknown;
    http.get(PathConstants.ORDERS).subscribe(r => (result = r));

    ctrl.expectOne(PathConstants.ORDERS).flush(null, {status: 401, statusText: 'Unauthorized'});
    const refresh = ctrl.expectOne(PathConstants.REFRESH);
    expect(refresh.request.body).toEqual({refresh_token: 'r1'});
    const fresh = jwt(future());
    refresh.flush({access_token: fresh, refresh_token: 'r2', token_type: 'Bearer', expires_in: 900});

    const retry = ctrl.expectOne(PathConstants.ORDERS);
    expect(retry.request.headers.get('Authorization')).toBe(`Bearer ${fresh}`);
    retry.flush({ok: true});
    expect(result).toEqual({ok: true});
    expect(localStorage.getItem('opa.refresh_token')).toBe('r2');
  });

  it('shares a single refresh between concurrent 401s (rotating refresh tokens)', () => {
    login(jwt(future()));
    http.get(PathConstants.ORDERS).subscribe();
    http.get(PathConstants.CATALOG_PRODUCTS).subscribe();

    ctrl.expectOne(PathConstants.ORDERS).flush(null, {status: 401, statusText: 'Unauthorized'});
    ctrl.expectOne(PathConstants.CATALOG_PRODUCTS).flush(null, {status: 401, statusText: 'Unauthorized'});

    const refreshes = ctrl.match(PathConstants.REFRESH);
    expect(refreshes.length).toBe(1);
    refreshes[0].flush({access_token: jwt(future()), refresh_token: 'r2', token_type: 'Bearer', expires_in: 900});

    ctrl.expectOne(PathConstants.ORDERS).flush({});
    ctrl.expectOne(PathConstants.CATALOG_PRODUCTS).flush([]);
  });

  it('refreshes proactively when the stored access token is expired', () => {
    login(jwt(Math.floor(Date.now() / 1000) - 60));
    http.get(PathConstants.ORDERS).subscribe();
    ctrl.expectNone(PathConstants.ORDERS);
    const fresh = jwt(future());
    ctrl.expectOne(PathConstants.REFRESH).flush({access_token: fresh, refresh_token: 'r2', token_type: 'Bearer', expires_in: 900});
    expect(ctrl.expectOne(PathConstants.ORDERS).request.headers.get('Authorization')).toBe(`Bearer ${fresh}`);
  });

  it('ends the session when refresh fails', () => {
    login(jwt(future()));
    let failed = false;
    http.get(PathConstants.ORDERS).subscribe({error: () => (failed = true)});
    ctrl.expectOne(PathConstants.ORDERS).flush(null, {status: 401, statusText: 'Unauthorized'});
    ctrl.expectOne(PathConstants.REFRESH).flush({code: 'REFRESH_TOKEN_EXPIRED'}, {status: 401, statusText: 'Unauthorized'});

    expect(failed).toBeTrue();
    expect(TestBed.inject(AuthService).isLoggedIn()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/sign-in'], jasmine.any(Object));
  });

  it('does not attach tokens to the login endpoint', () => {
    login(jwt(future()));
    http.post(PathConstants.LOGIN, {}).subscribe();
    expect(ctrl.expectOne(PathConstants.LOGIN).request.headers.has('Authorization')).toBeFalse();
  });
});
