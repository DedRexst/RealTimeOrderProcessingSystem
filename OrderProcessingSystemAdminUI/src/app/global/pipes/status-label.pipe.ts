import {Pipe, PipeTransform} from '@angular/core';
import {statusMeta} from '../../utils/constants/order-status-constants';

@Pipe({name: 'statusLabel'})
export class StatusLabelPipe implements PipeTransform {
  transform(status: string | null | undefined): string {
    return status ? statusMeta(status).label : '—';
  }
}
