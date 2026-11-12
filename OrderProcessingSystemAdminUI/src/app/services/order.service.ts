import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {CreateOrderRequest, OrderQuery, OrderResponse, OrderSummary, UpdateOrderStatusRequest} from './models/order-models';
import {PageResponse} from './models/page-models';
import {PathConstants} from '../utils/constants/path-constant';

@Injectable({providedIn: 'root'})
export class OrderService {
  constructor(private http: HttpClient) {
  }

  list(query: OrderQuery): Observable<PageResponse<OrderSummary>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size);
    if (query.status) {
      params = params.set('status', query.status);
    }
    if (query.customerId != null) {
      params = params.set('customerId', query.customerId);
    }
    return this.http.get<PageResponse<OrderSummary>>(PathConstants.ORDERS, {params});
  }

  get(id: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${PathConstants.ORDERS}/${id}`);
  }

  create(request: CreateOrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(PathConstants.ORDERS, request);
  }

  updateStatus(id: number, request: UpdateOrderStatusRequest): Observable<OrderResponse> {
    return this.http.patch<OrderResponse>(`${PathConstants.ORDERS}/${id}/status`, request);
  }
}
