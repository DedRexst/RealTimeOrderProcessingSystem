export enum AlertType {
  SUCCESS,
  ERROR,
  INFO,
  WARNING
}

export class Alert {
  id = 'default-alert';
  uid = 0;
  type?: AlertType;
  title?: string;
  message = '';
  autoClose = true;

  constructor(init?: Partial<Alert>) {
    Object.assign(this, init);
  }
}
