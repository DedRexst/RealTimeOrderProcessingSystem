import {HttpErrorResponse} from '@angular/common/http';

export interface ApiError {
  status: number;
  message: string;
  code?: string;
  fieldErrors: Record<string, string>;
}

const CODE_MESSAGES: Record<string, string> = {
  INVALID_EMAIL: 'Invalid email or password.',
  INVALID_PASSWORD: 'Invalid email or password.',
  USER_DELETED: 'This account has been deactivated.',
  ACCOUNT_INACTIVE: 'This account is inactive.',
  EMAIL_ALREADY_USED: 'This email is already in use.',
  REFRESH_TOKEN_EXPIRED: 'Your session has expired. Please sign in again.',
  REFRESH_TOKEN_ALREADY_USED: 'Your session was revoked. Please sign in again.',
  REFRESH_TOKEN_NOT_RECOGNISED: 'Your session is no longer valid. Please sign in again.'
};

export function parseApiError(err: unknown): ApiError {
  if (!(err instanceof HttpErrorResponse)) {
    return {status: 0, message: err instanceof Error ? err.message : 'Unexpected error', fieldErrors: {}};
  }
  if (err.status === 0) {
    return {status: 0, message: 'Service unreachable. Is the backend running?', fieldErrors: {}};
  }

  const body = typeof err.error === 'object' && err.error !== null ? err.error as Record<string, unknown> : {};
  const fieldErrors = (body['errors'] && typeof body['errors'] === 'object')
    ? body['errors'] as Record<string, string>
    : {};
  const code = typeof body['code'] === 'string' ? body['code'] : undefined;

  let message: string | undefined =
    (code && CODE_MESSAGES[code]) ||
    (body['error_description'] as string) ||
    (body['detail'] as string) ||
    (body['title'] as string) ||
    (body['error'] as string) ||
    (typeof err.error === 'string' && err.error.length < 300 ? err.error : undefined);

  if (!message) {
    message = defaultMessage(err.status);
  }
  if (Object.keys(fieldErrors).length && message === 'Request validation failed') {
    message = Object.entries(fieldErrors).map(([f, m]) => `${f}: ${m}`).join(' · ');
  }
  return {status: err.status, message, code, fieldErrors};
}

function defaultMessage(status: number): string {
  switch (status) {
    case 400: return 'The request was invalid.';
    case 401: return 'You are not signed in.';
    case 403: return 'You do not have permission to do that.';
    case 404: return 'Not found.';
    case 409: return 'Conflict with the current state.';
    default: return status >= 500 ? 'Server error. Please try again.' : `Request failed (${status}).`;
  }
}
