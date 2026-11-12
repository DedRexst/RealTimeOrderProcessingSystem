import {HttpErrorResponse} from '@angular/common/http';
import {parseApiError} from './error-util';

const http = (status: number, error: unknown) => new HttpErrorResponse({status, error});

describe('parseApiError', () => {
  it('reads ProblemDetail.detail', () => {
    const e = parseApiError(http(409, {title: 'Conflict', status: 409, detail: 'Order 5 cannot move from FAILED to RESERVED'}));
    expect(e.message).toBe('Order 5 cannot move from FAILED to RESERVED');
  });

  it('maps ProblemDetail business codes to friendly messages', () => {
    const e = parseApiError(http(401, {detail: 'bad', code: 'INVALID_PASSWORD'}));
    expect(e.code).toBe('INVALID_PASSWORD');
    expect(e.message).toBe('Invalid email or password.');
  });

  it('exposes validation field errors', () => {
    const e = parseApiError(http(400, {detail: 'Request validation failed', errors: {name: 'must not be blank'}}));
    expect(e.fieldErrors).toEqual({name: 'must not be blank'});
    expect(e.message).toContain('name: must not be blank');
  });

  it('reads OAuth2 error_description', () => {
    const e = parseApiError(http(400, {error: 'invalid_scope', error_description: 'Scope not allowed'}));
    expect(e.message).toBe('Scope not allowed');
  });

  it('reports network failures', () => {
    expect(parseApiError(http(0, null)).message).toContain('unreachable');
  });

  it('falls back to a status-based message for empty bodies', () => {
    expect(parseApiError(http(403, null)).message).toContain('permission');
  });
});
