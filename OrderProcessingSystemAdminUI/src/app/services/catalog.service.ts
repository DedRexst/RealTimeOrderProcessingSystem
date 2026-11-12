import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError, map, shareReplay, tap} from 'rxjs/operators';
import {CreateProductRequest, CustomerRef, ProductRef} from './models/catalog-models';
import {PathConstants} from '../utils/constants/path-constant';

@Injectable({providedIn: 'root'})
export class CatalogService {
  private products$?: Observable<ProductRef[]>;
  private customers$?: Observable<CustomerRef[]>;

  constructor(private http: HttpClient) {
  }

  products(): Observable<ProductRef[]> {
    if (!this.products$) {
      this.products$ = this.http.get<ProductRef[]>(PathConstants.CATALOG_PRODUCTS).pipe(
        catchError(err => {
          this.products$ = undefined;
          return throwError(() => err);
        }),
        shareReplay({bufferSize: 1, refCount: false})
      );
    }
    return this.products$;
  }

  customers(): Observable<CustomerRef[]> {
    if (!this.customers$) {
      this.customers$ = this.http.get<CustomerRef[]>(PathConstants.CATALOG_CUSTOMERS).pipe(
        catchError(err => {
          this.customers$ = undefined;
          return throwError(() => err);
        }),
        shareReplay({bufferSize: 1, refCount: false})
      );
    }
    return this.customers$;
  }

  createProduct(request: CreateProductRequest): Observable<ProductRef> {
    return this.http.post<ProductRef>(PathConstants.CATALOG_PRODUCTS, request).pipe(
      tap(() => (this.products$ = undefined))
    );
  }

  productMap(): Observable<Map<number, ProductRef>> {
    return this.products().pipe(map(list => new Map(list.map(p => [p.id, p]))));
  }

  customerMap(): Observable<Map<number, CustomerRef>> {
    return this.customers().pipe(map(list => new Map(list.map(c => [c.id, c]))));
  }

  invalidate(): void {
    this.products$ = undefined;
    this.customers$ = undefined;
  }
}
