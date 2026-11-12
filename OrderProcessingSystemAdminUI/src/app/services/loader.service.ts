import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class LoaderService {
  readonly countLoading$ = new BehaviorSubject<number>(0);
  readonly loading$ = this.countLoading$.pipe(map(c => c > 0));

  showLoader(): void {
    this.countLoading$.next(this.countLoading$.value + 1);
  }

  hideLoader(): void {
    this.countLoading$.next(Math.max(0, this.countLoading$.value - 1));
  }
}
