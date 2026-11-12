import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'duration'})
export class DurationPipe implements PipeTransform {
  transform(seconds: number | null | undefined): string {
    if (seconds == null || Number.isNaN(seconds)) {
      return '—';
    }
    if (seconds < 1) {
      return `${Math.round(seconds * 1000)}ms`;
    }
    if (seconds < 60) {
      return `${seconds.toFixed(seconds < 10 ? 1 : 0)}s`;
    }
    const m = Math.floor(seconds / 60);
    const s = Math.round(seconds % 60);
    if (m < 60) {
      return `${m}m ${String(s).padStart(2, '0')}s`;
    }
    const h = Math.floor(m / 60);
    return `${h}h ${m % 60}m`;
  }
}
