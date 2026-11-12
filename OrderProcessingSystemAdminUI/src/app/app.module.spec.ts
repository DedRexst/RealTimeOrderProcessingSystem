import {TestBed} from '@angular/core/testing';
import {Chart} from 'chart.js';
import {AppModule} from './app.module';
import {AppComponent} from './app.component';

describe('AppModule', () => {
  it('loads and creates the root component', () => {
    TestBed.configureTestingModule({imports: [AppModule]});
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('applies the shared chart theme', () => {
    expect(Chart.defaults.plugins.legend.labels.usePointStyle).toBeTrue();
    expect(Chart.defaults.font.family).toContain('Inter');
  });
});
