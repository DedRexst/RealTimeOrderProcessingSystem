import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserDto} from './models/auth-models';
import {PathConstants} from '../utils/constants/path-constant';

@Injectable({providedIn: 'root'})
export class UserService {
  constructor(private http: HttpClient) {
  }

  me(): Observable<UserDto> {
    return this.http.get<UserDto>(PathConstants.ME);
  }
}
