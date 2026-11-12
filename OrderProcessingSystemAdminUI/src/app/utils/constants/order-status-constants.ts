import {OrderStatus} from '../../services/models/order-models';

export const ALLOWED_TRANSITIONS: Record<OrderStatus, OrderStatus[]> = {
  PENDING: ['RESERVED', 'FAILED'],
  RESERVED: ['SUCCESSFUL', 'RELEASED', 'FAILED'],
  RELEASED: ['FAILED'],
  SUCCESSFUL: [],
  FAILED: []
};

export function nextStatuses(current: OrderStatus | null | undefined): OrderStatus[] {
  return ALLOWED_TRANSITIONS[current ?? 'PENDING'] ?? [];
}

export function isFinal(status: OrderStatus): boolean {
  return nextStatuses(status).length === 0;
}

export interface StatusMeta {
  label: string;
  icon: string;
  color: string;
  description: string;
}

export const STATUS_META: Record<OrderStatus, StatusMeta> = {
  PENDING: {label: 'Pending', icon: 'bi-hourglass-split', color: '#d97706', description: 'Awaiting inventory reservation'},
  RESERVED: {label: 'Reserved', icon: 'bi-box-seam', color: '#2563eb', description: 'Stock reserved, ready to fulfil'},
  SUCCESSFUL: {label: 'Successful', icon: 'bi-check2-circle', color: '#059669', description: 'Order completed'},
  RELEASED: {label: 'Released', icon: 'bi-arrow-counterclockwise', color: '#7c3aed', description: 'Reservation released'},
  FAILED: {label: 'Failed', icon: 'bi-x-octagon', color: '#dc2626', description: 'Order failed'}
};

export function statusMeta(status: string | null | undefined): StatusMeta {
  return STATUS_META[(status ?? 'PENDING') as OrderStatus] ??
    {label: status ?? '—', icon: 'bi-question-circle', color: '#64748b', description: ''};
}
