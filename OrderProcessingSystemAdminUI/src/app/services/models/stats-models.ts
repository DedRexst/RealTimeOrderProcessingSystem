export type StatsMessageType =
  | 'CUSTOMER_STATS'
  | 'ORDER_RATE'
  | 'CURRENCY_BREAKDOWN'
  | 'PRODUCT_POPULARITY'
  | 'ORDER_STATUS_CHANGED'
  | 'STATUS_TRANSITION_TIME'
  | 'STATUS_FUNNEL'
  | 'STATUS_REASONS';

export interface StatsMessage<T = unknown> {
  type: StatsMessageType;
  data: T;
}

export interface CustomerStats {
  customerId: string;
  orderCount: number;
  productCount: number;
  avgProductsPerOrder: number;
}

export interface OrderRate {
  windowKey: string;
  orderCount: number;
}

export interface CurrencyBreakdown {
  currency: string;
  orderCount: number;
}

export interface ProductPopularity {
  productId: string;
  orderCount: number;
}

export interface OrderStatusChanged {
  orderId: number;
  customerId: number;
  previousStatus: string | null;
  newStatus: string;
  currency: string | null;
  productIds: number[] | null;
  reason: string | null;
  changedBy: string | null;
  externalReference: string | null;
  occurredAt: string;
}

export interface TransitionTime {
  fromStatus: string;
  toStatus: string;
  samples: number;
  avgSeconds: number;
  maxSeconds: number;
  lastSeconds: number;
}

export interface FunnelStage {
  status: string;
  orders: number;
  sharePercent: number;
}

export interface StatusFunnel {
  totalOrders: number;
  stages: FunnelStage[];
}

export interface ReasonCount {
  reason: string;
  count: number;
}

export interface StatusReasons {
  status: string;
  totalWithReason: number;
  topReasons: ReasonCount[];
}

export type SocketStatus = 'connecting' | 'live' | 'offline';
