import {NgModule} from '@angular/core';
import {BaseChartDirective, provideCharts, withDefaultRegisterables} from 'ng2-charts';
import {Chart, registerables} from 'chart.js';

Chart.register(...registerables);

Chart.defaults.font.family = `'Inter', system-ui, sans-serif`;
Chart.defaults.font.size = 12;
Chart.defaults.color = '#64748b';
Chart.defaults.borderColor = '#eef1f6';
Chart.defaults.plugins.legend.labels.usePointStyle = true;
Chart.defaults.plugins.legend.labels.boxWidth = 8;
Chart.defaults.plugins.tooltip.backgroundColor = '#0f172a';
Chart.defaults.plugins.tooltip.padding = 10;
Chart.defaults.plugins.tooltip.cornerRadius = 8;
Chart.defaults.plugins.tooltip.titleFont = {weight: 600};
Chart.defaults.plugins.tooltip.displayColors = true;
Chart.defaults.plugins.tooltip.boxPadding = 4;
Chart.defaults.animation = {duration: 450, easing: 'easeOutQuart'};
Chart.defaults.maintainAspectRatio = false;

@NgModule({
  imports: [BaseChartDirective],
  exports: [BaseChartDirective],
  providers: [provideCharts(withDefaultRegisterables())]
})
export class Ng2ChartModule {
}
