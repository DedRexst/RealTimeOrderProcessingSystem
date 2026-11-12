export const environment = {
  production: true
};

export class Constants {
  static get Default() {
    return {
      AUTH_ENDPOINT: 'http://localhost:8085',
      ORDER_ENDPOINT: 'http://localhost:8082',
      ANALYTICS_WS: 'ws://localhost:8084/ws/stats'
    };
  }
}
