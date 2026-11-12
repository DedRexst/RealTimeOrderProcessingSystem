import {INITIAL_ANALYTICS_STATE, MAX_EVENTS, MAX_RATE_WINDOWS, reduceAnalytics} from './analytics-store.service';
import {OrderStatusChanged, StatsMessage} from './models/stats-models';

function evt(orderId: number, newStatus: string, occurredAt: string, previousStatus: string | null = null): StatsMessage {
  const data: OrderStatusChanged = {
    orderId, customerId: 1, previousStatus, newStatus, currency: 'USD', productIds: [1],
    reason: null, changedBy: null, externalReference: null, occurredAt
  };
  return {type: 'ORDER_STATUS_CHANGED', data};
}

describe('reduceAnalytics', () => {
  it('replaces snapshot values for the same key', () => {
    let s = reduceAnalytics(INITIAL_ANALYTICS_STATE, {type: 'CURRENCY_BREAKDOWN', data: {currency: 'USD', orderCount: 1}});
    s = reduceAnalytics(s, {type: 'CURRENCY_BREAKDOWN', data: {currency: 'USD', orderCount: 5}});
    s = reduceAnalytics(s, {type: 'CURRENCY_BREAKDOWN', data: {currency: 'EUR', orderCount: 2}});
    expect(Object.keys(s.currencies).length).toBe(2);
    expect(s.currencies['USD'].orderCount).toBe(5);
  });

  it('keys transition times by from->to', () => {
    const s = reduceAnalytics(INITIAL_ANALYTICS_STATE, {
      type: 'STATUS_TRANSITION_TIME',
      data: {fromStatus: 'PENDING', toStatus: 'RESERVED', samples: 3, avgSeconds: 1.2, maxSeconds: 2, lastSeconds: 1}
    });
    expect(s.transitions['PENDING->RESERVED'].samples).toBe(3);
  });

  it('keeps order-rate windows sorted, de-duplicated and capped', () => {
    let s = INITIAL_ANALYTICS_STATE;
    const base = Date.parse('2026-01-01T00:00:00Z');
    for (let i = MAX_RATE_WINDOWS + 10; i >= 0; i--) {
      s = reduceAnalytics(s, {type: 'ORDER_RATE', data: {windowKey: new Date(base + i * 60000).toISOString(), orderCount: i}});
    }
    s = reduceAnalytics(s, {type: 'ORDER_RATE', data: {windowKey: new Date(base + (MAX_RATE_WINDOWS + 10) * 60000).toISOString(), orderCount: 999}});
    expect(s.orderRate.length).toBe(MAX_RATE_WINDOWS);
    expect(s.orderRate.at(-1)!.orderCount).toBe(999);
    const times = s.orderRate.map(r => Date.parse(r.windowKey));
    expect([...times].sort((a, b) => a - b)).toEqual(times);
  });

  it('ignores replayed events (idempotent reconnect) and returns the same state reference', () => {
    const s1 = reduceAnalytics(INITIAL_ANALYTICS_STATE, evt(7, 'RESERVED', '2026-01-01T00:00:01Z', 'PENDING'));
    const s2 = reduceAnalytics(s1, evt(7, 'RESERVED', '2026-01-01T00:00:01Z', 'PENDING'));
    expect(s2).toBe(s1);
    expect(s2.events.length).toBe(1);
  });

  it('orders events newest first and caps the ring buffer', () => {
    let s = INITIAL_ANALYTICS_STATE;
    for (let i = 0; i < MAX_EVENTS + 25; i++) {
      s = reduceAnalytics(s, evt(i, 'PENDING', new Date(Date.UTC(2026, 0, 1, 0, 0, i)).toISOString()));
    }
    expect(s.events.length).toBe(MAX_EVENTS);
    expect(s.events[0].orderId).toBe(MAX_EVENTS + 24);
  });

  it('ignores unknown message types', () => {
    const s = reduceAnalytics(INITIAL_ANALYTICS_STATE, {type: 'NOPE' as never, data: {}});
    expect(s).toBe(INITIAL_ANALYTICS_STATE);
  });
});
