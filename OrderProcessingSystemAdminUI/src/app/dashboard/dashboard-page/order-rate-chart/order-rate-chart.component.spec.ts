import {fillRateWindows} from './order-rate-chart.component';

describe('fillRateWindows', () => {
  const t0 = Date.parse('2026-01-01T10:00:00Z');
  const iso = (m: number) => new Date(t0 + m * 60000).toISOString();

  it('returns nothing without data', () => {
    expect(fillRateWindows([], t0)).toEqual([]);
  });

  it('fills gaps with zeros and extends to the current window', () => {
    const pts = fillRateWindows([{windowKey: iso(0), orderCount: 3}, {windowKey: iso(2), orderCount: 1}], t0 + 4 * 60000 + 5000, 6);
    expect(pts.map(p => p.v)).toEqual([0, 3, 0, 1, 0, 0]);
    expect(pts.at(-1)!.t).toBe(t0 + 4 * 60000);
  });
});
