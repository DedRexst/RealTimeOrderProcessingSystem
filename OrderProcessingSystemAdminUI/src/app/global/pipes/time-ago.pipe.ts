import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'timeAgo', pure: false})
export class TimeAgoPipe implements PipeTransform {
  transform(value: string | number | Date | null | undefined): string {
    if (value == null) {
      return '—';
    }
    const ts = value instanceof Date ? value.getTime() : typeof value === 'number' ? value : Date.parse(value);
    if (Number.isNaN(ts)) {
      return '—';
    }
    const diff = Math.round((Date.now() - ts) / 1000);
    if (diff < 5) {
      return 'just now';
    }
    if (diff < 60) {
      return `${diff}s ago`;
    }
    if (diff < 3600) {
      return `${Math.floor(diff / 60)}m ago`;
    }
    if (diff < 86400) {
      return `${Math.floor(diff / 3600)}h ago`;
    }
    return `${Math.floor(diff / 86400)}d ago`;
  }
}
