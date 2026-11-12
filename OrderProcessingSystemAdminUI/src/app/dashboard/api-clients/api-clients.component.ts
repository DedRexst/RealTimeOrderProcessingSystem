import {Component, OnInit} from '@angular/core';
import {BsModalService} from 'ngx-bootstrap/modal';
import {finalize} from 'rxjs/operators';
import {ClientService} from '../../services/client.service';
import {ClientCredentials, ClientDto} from '../../services/models/client-models';
import {AlertService} from '../../alert/alert.service';
import {parseApiError} from '../../utils/error-util';
import {RegisterClientModalComponent} from './register-client-modal/register-client-modal.component';
import {ClientSecretModalComponent} from './client-secret-modal/client-secret-modal.component';

@Component({
  selector: 'app-api-clients',
  templateUrl: './api-clients.component.html',
  styleUrls: ['./api-clients.component.scss']
})
export class ApiClientsComponent implements OnInit {
  clients: ClientDto[] = [];
  loading = true;
  error: string | null = null;
  busy = new Set<string>();
  confirmRotate: string | null = null;

  constructor(private service: ClientService,
              private modal: BsModalService,
              private alerts: AlertService) {
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.service.list().pipe(finalize(() => (this.loading = false))).subscribe({
      next: list => (this.clients = [...list].sort((a, b) => Date.parse(b.createdTime) - Date.parse(a.createdTime))),
      error: err => (this.error = parseApiError(err).message)
    });
  }

  get enabledCount(): number {
    return this.clients.filter(c => c.enabled).length;
  }

  register(): void {
    const ref = this.modal.show(RegisterClientModalComponent, {class: 'modal-dialog-centered', ignoreBackdropClick: true});
    ref.content?.registered.subscribe(creds => {
      this.upsert(creds.client);
      this.showSecret(creds, 'Client registered');
    });
  }

  toggle(client: ClientDto): void {
    this.busy.add(client.clientId);
    this.service.setEnabled(client.clientId, !client.enabled)
      .pipe(finalize(() => this.busy.delete(client.clientId)))
      .subscribe({
        next: updated => {
          this.upsert(updated);
          this.alerts.success(`${updated.name} ${updated.enabled ? 'enabled' : 'disabled'}.`);
        },
        error: err => this.alerts.error(parseApiError(err).message)
      });
  }

  rotate(client: ClientDto): void {
    this.confirmRotate = null;
    this.busy.add(client.clientId);
    this.service.rotateSecret(client.clientId)
      .pipe(finalize(() => this.busy.delete(client.clientId)))
      .subscribe({
        next: creds => {
          this.upsert(creds.client);
          this.showSecret(creds, 'Secret rotated');
        },
        error: err => this.alerts.error(parseApiError(err).message)
      });
  }

  copy(text: string): void {
    navigator.clipboard?.writeText(text).then(() => this.alerts.info('Copied to clipboard.'));
  }

  private showSecret(creds: ClientCredentials, title: string): void {
    this.modal.show(ClientSecretModalComponent, {
      class: 'modal-dialog-centered',
      ignoreBackdropClick: true,
      keyboard: false,
      initialState: {credentials: creds, title}
    });
  }

  private upsert(client: ClientDto): void {
    const i = this.clients.findIndex(c => c.clientId === client.clientId);
    this.clients = i === -1 ? [client, ...this.clients] : this.clients.map(c => (c.clientId === client.clientId ? client : c));
  }
}
