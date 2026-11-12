import {ALLOWED_TRANSITIONS, isFinal, nextStatuses} from './order-status-constants';

describe('order status transitions (mirror of OrderServiceImpl.ALLOWED_TRANSITIONS)', () => {
  it('matches the backend state machine', () => {
    expect(ALLOWED_TRANSITIONS).toEqual({
      PENDING: ['RESERVED', 'FAILED'],
      RESERVED: ['SUCCESSFUL', 'RELEASED', 'FAILED'],
      RELEASED: ['FAILED'],
      SUCCESSFUL: [],
      FAILED: []
    });
  });

  it('treats a null status as PENDING, like the backend', () => {
    expect(nextStatuses(null)).toEqual(['RESERVED', 'FAILED']);
  });

  it('marks SUCCESSFUL and FAILED as final', () => {
    expect(isFinal('SUCCESSFUL')).toBeTrue();
    expect(isFinal('FAILED')).toBeTrue();
    expect(isFinal('RELEASED')).toBeFalse();
  });
});
