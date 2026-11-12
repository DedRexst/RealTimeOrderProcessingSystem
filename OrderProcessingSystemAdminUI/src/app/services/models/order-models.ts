export type OrderStatus = 'PENDING' | 'RESERVED' | 'SUCCESSFUL' | 'RELEASED' | 'FAILED';
export type Currency = 'UAH' | 'USD' | 'EUR';

export const ORDER_STATUSES: OrderStatus[] = ['PENDING', 'RESERVED', 'SUCCESSFUL', 'RELEASED', 'FAILED'];
export const CURRENCIES: Currency[] = ['UAH', 'USD', 'EUR'];

export interface OrderSummary {
  id: number;
  customerId: number;
  customerName: string;
  productIds: number[];
  currency: Currency;
  address: string;
  status: OrderStatus;
  createdTime: string;
  updatedTime: string;
}

export interface OrderResponse {
  id: number;
  customerId: number;
  productIds: number[];
  currency: Currency;
  address: string;
  status: OrderStatus;
}

export interface CreateOrderRequest {
  customerId: number;
  productIds: number[];
  currency: Currency;
  address: string;
}

export interface UpdateOrderStatusRequest {
  status: OrderStatus;
  reason?: string;
  changedBy?: string;
}

export interface OrderQuery {
  status?: OrderStatus | null;
  customerId?: number | null;
  page: number;
  size: number;
}
