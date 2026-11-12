import {Component, Input} from '@angular/core';
import {OrderStatusChanged} from '../../../services/models/stats-models';
import {eventKey} from '../../../services/analytics-store.service';

@Component({
  selector: 'app-live-feed',
  templateUrl: './live-feed.component.html',
  styleUrls: ['./live-feed.component.scss']
})
export class LiveFeedComponent {
  @Input() events: OrderStatusChanged[] = [];
  @Input() limit = 12;

  trackByKey(_: number, e: OrderStatusChanged): string {
    return eventKey(e);
  }
}
