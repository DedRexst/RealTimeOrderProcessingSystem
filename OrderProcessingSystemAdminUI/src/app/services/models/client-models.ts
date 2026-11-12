export interface ClientDto {
  clientId: string;
  name: string;
  description?: string;
  contactEmail?: string;
  scopes: string[];
  enabled: boolean;
  createdTime: string;
  secretRotatedAt: string;
}

export interface RegisterClientRequest {
  name: string;
  description?: string;
  contactEmail?: string;
  scopes: string[];
}

export interface ClientCredentials {
  client: ClientDto;
  clientSecret: string;
  note: string;
}

export const SCOPE_PATTERN = /^[a-z][a-z0-9_:.-]{1,63}$/;
export const SUGGESTED_SCOPES = ['orders:write', 'orders:read', 'tokens:introspect'];
