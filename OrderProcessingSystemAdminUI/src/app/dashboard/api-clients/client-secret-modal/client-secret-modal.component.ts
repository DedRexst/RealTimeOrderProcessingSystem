import {Component, Input} from '@angular/core';
import {BsModalRef} from 'ngx-bootstrap/modal';
import {ClientCredentials} from '../../../services/models/client-models';
import {Constants} from '../../../../environments/environment';

@Component({
  selector: 'app-client-secret-modal',
  templateUrl: './client-secret-modal.component.html'
})
export class ClientSecretModalComponent {
  @Input({required: true}) credentials!: ClientCredentials;
  @Input() title = 'Client credentials';

  revealed = false;
  copied: 'id' | 'secret' | 'curl' | null = null;
  acknowledged = false;
  readonly tokenUrl = `${Constants.Default.AUTH_ENDPOINT}/oauth2/token`;

  constructor(public modalRef: BsModalRef) {
  }

  get curl(): string {
    const c = this.credentials;
    return `curl -s -X POST ${this.tokenUrl} \\\n  -u '${c.client.clientId}:${c.clientSecret}' \\\n  -d grant_type=client_credentials -d scope='${c.client.scopes.join(' ')}'`;
  }

  copy(what: 'id' | 'secret' | 'curl'): void {
    const text = what === 'id' ? this.credentials.client.clientId : what === 'secret' ? this.credentials.clientSecret : this.curl;
    navigator.clipboard?.writeText(text).then(() => {
      this.copied = what;
      setTimeout(() => (this.copied = null), 1500);
    });
  }
}
