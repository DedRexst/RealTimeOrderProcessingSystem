import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ModalModule} from 'ngx-bootstrap/modal';
import {TooltipModule} from 'ngx-bootstrap/tooltip';
import {BsDropdownModule} from 'ngx-bootstrap/dropdown';

import {AppComponent} from './app.component';
import {AppRoutingModule} from './module/app-routing.module';
import {Ng2ChartModule} from './module/ng2-chart.module';
import {AuthInterceptor} from './authInterceptor';
import {LoadingInterceptor} from './services/LoadingInterceptor';
import {AlertComponent} from './alert/alert.component';

import {SigninComponent} from './account/signin/signin.component';
import {ContainerComponent} from './dashboard/container/container.component';
import {NavbarComponent} from './dashboard/navbar/navbar.component';
import {TopbarComponent} from './dashboard/topbar/topbar.component';
import {AccessRestrictedComponent} from './dashboard/access-restricted/access-restricted.component';

import {DashboardComponent} from './dashboard/dashboard-page/dashboard.component';
import {KpiTilesComponent} from './dashboard/dashboard-page/kpi-tiles/kpi-tiles.component';
import {OrderRateChartComponent} from './dashboard/dashboard-page/order-rate-chart/order-rate-chart.component';
import {CurrencyChartComponent} from './dashboard/dashboard-page/currency-chart/currency-chart.component';
import {StatusFunnelChartComponent} from './dashboard/dashboard-page/status-funnel-chart/status-funnel-chart.component';
import {TopProductsComponent} from './dashboard/dashboard-page/top-products/top-products.component';
import {TransitionTimesComponent} from './dashboard/dashboard-page/transition-times/transition-times.component';
import {StatusReasonsComponent} from './dashboard/dashboard-page/status-reasons/status-reasons.component';
import {LiveFeedComponent} from './dashboard/dashboard-page/live-feed/live-feed.component';

import {OrdersComponent} from './dashboard/orders/orders.component';
import {OrderDetailsComponent} from './dashboard/orders/order-details/order-details.component';
import {CreateOrderModalComponent} from './dashboard/orders/create-order-modal/create-order-modal.component';
import {ChangeStatusModalComponent} from './dashboard/orders/change-status-modal/change-status-modal.component';
import {CatalogComponent} from './dashboard/catalog/catalog.component';
import {AddProductModalComponent} from './dashboard/catalog/add-product-modal/add-product-modal.component';
import {ApiClientsComponent} from './dashboard/api-clients/api-clients.component';
import {RegisterClientModalComponent} from './dashboard/api-clients/register-client-modal/register-client-modal.component';
import {ClientSecretModalComponent} from './dashboard/api-clients/client-secret-modal/client-secret-modal.component';
import {ProfileComponent} from './dashboard/profile/profile.component';

import {StatusBadgeComponent} from './global/components/status-badge/status-badge.component';
import {TimeAgoPipe} from './global/pipes/time-ago.pipe';
import {DurationPipe} from './global/pipes/duration.pipe';
import {ProductNamePipe} from './global/pipes/product-name.pipe';
import {StatusLabelPipe} from './global/pipes/status-label.pipe';

@NgModule({
  declarations: [
    AppComponent,
    AlertComponent,
    SigninComponent,
    ContainerComponent,
    NavbarComponent,
    TopbarComponent,
    AccessRestrictedComponent,
    DashboardComponent,
    KpiTilesComponent,
    OrderRateChartComponent,
    CurrencyChartComponent,
    StatusFunnelChartComponent,
    TopProductsComponent,
    TransitionTimesComponent,
    StatusReasonsComponent,
    LiveFeedComponent,
    OrdersComponent,
    OrderDetailsComponent,
    CreateOrderModalComponent,
    ChangeStatusModalComponent,
    CatalogComponent,
    AddProductModalComponent,
    ApiClientsComponent,
    RegisterClientModalComponent,
    ClientSecretModalComponent,
    ProfileComponent,
    StatusBadgeComponent,
    TimeAgoPipe,
    DurationPipe,
    ProductNamePipe,
    StatusLabelPipe
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    Ng2ChartModule,
    ModalModule.forRoot(),
    TooltipModule.forRoot(),
    BsDropdownModule.forRoot()
  ],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    {provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
