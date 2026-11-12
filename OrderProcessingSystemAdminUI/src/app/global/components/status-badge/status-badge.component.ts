import {Component, Input} from '@angular/core';
import {statusMeta} from '../../../utils/constants/order-status-constants';

@Component({
  selector: 'app-status-badge',
  template: `<span class="status-badge status-{{ status?.toLowerCase() }}" [title]="meta.description">{{ meta.label }}</span>`
})
export class StatusBadgeComponent {
  @Input() status: string | null | undefined;

  get meta() {
    return statusMeta(this.status);
  }
}
