import {Constants} from '../../../environments/environment';

const AUTH = Constants.Default.AUTH_ENDPOINT;
const ORDER = Constants.Default.ORDER_ENDPOINT;

export const PathConstants = {
  LOGIN: `${AUTH}/api/v1/auth/login`,
  REFRESH: `${AUTH}/api/v1/auth/refresh`,
  LOGOUT: `${AUTH}/api/v1/auth/logout`,
  ME: `${AUTH}/api/v1/users/me`,
  CLIENTS: `${AUTH}/api/v1/clients`,
  ORDERS: `${ORDER}/api/v1/orders`,
  CATALOG_PRODUCTS: `${ORDER}/api/v1/catalog/products`,
  CATALOG_CUSTOMERS: `${ORDER}/api/v1/catalog/customers`
};

export const PUBLIC_AUTH_PATHS = [PathConstants.LOGIN, PathConstants.REFRESH, PathConstants.LOGOUT];

export const API_ORIGINS = [AUTH, ORDER];
