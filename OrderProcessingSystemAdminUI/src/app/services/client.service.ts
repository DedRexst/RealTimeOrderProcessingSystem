import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ClientCredentials, ClientDto, RegisterClientRequest} from './models/client-models';
import {PathConstants} from '../utils/constants/path-constant';

@Injectable({providedIn: 'root'})
export class ClientService {
  constructor(private http: HttpClient) {
  }

  list(): Observable<ClientDto[]> {
    return this.http.get<ClientDto[]>(PathConstants.CLIENTS);
  }

  register(request: RegisterClientRequest): Observable<ClientCredentials> {
    return this.http.post<ClientCredentials>(PathConstants.CLIENTS, request);
  }

  rotateSecret(clientId: string): Observable<ClientCredentials> {
    return this.http.post<ClientCredentials>(`${PathConstants.CLIENTS}/${encodeURIComponent(clientId)}/secret`, null);
  }

  setEnabled(clientId: string, enabled: boolean): Observable<ClientDto> {
    const action = enabled ? 'enable' : 'disable';
    return this.http.post<ClientDto>(`${PathConstants.CLIENTS}/${encodeURIComponent(clientId)}/${action}`, null);
  }
}
