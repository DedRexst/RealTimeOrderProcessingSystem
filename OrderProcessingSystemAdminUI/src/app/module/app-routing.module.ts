import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../guard/auth.guard';
import {SigninGuard} from '../guard/signin.guard';
import {AdminGuard} from '../guard/admin.guard';
import {SigninComponent} from '../account/signin/signin.component';
import {ContainerComponent} from '../dashboard/container/container.component';
import {DashboardComponent} from '../dashboard/dashboard-page/dashboard.component';
import {OrdersComponent} from '../dashboard/orders/orders.component';
import {OrderDetailsComponent} from '../dashboard/orders/order-details/order-details.component';
import {CatalogComponent} from '../dashboard/catalog/catalog.component';
import {ApiClientsComponent} from '../dashboard/api-clients/api-clients.component';
import {ProfileComponent} from '../dashboard/profile/profile.component';
import {AccessRestrictedComponent} from '../dashboard/access-restricted/access-restricted.component';

const routes: Routes = [
  {path: 'sign-in', component: SigninComponent, canActivate: [SigninGuard], title: 'Sign in · OrderFlow'},
  {
    path: '',
    component: ContainerComponent,
    canActivate: [AuthGuard],
    children: [
      {path: '', pathMatch: 'full', redirectTo: 'dashboard'},
      {path: 'dashboard', component: DashboardComponent, data: {title: 'Dashboard'}, title: 'Dashboard · OrderFlow'},
      {path: 'orders', component: OrdersComponent, data: {title: 'Orders'}, title: 'Orders · OrderFlow'},
      {path: 'orders/:id', component: OrderDetailsComponent, data: {title: 'Order details'}, title: 'Order · OrderFlow'},
      {path: 'catalog', component: CatalogComponent, data: {title: 'Catalog'}, title: 'Catalog · OrderFlow'},
      {path: 'api-clients', component: ApiClientsComponent, canActivate: [AdminGuard], data: {title: 'API clients'}, title: 'API clients · OrderFlow'},
      {path: 'profile', component: ProfileComponent, data: {title: 'Profile'}, title: 'Profile · OrderFlow'},
      {path: 'access-restricted', component: AccessRestrictedComponent, data: {title: 'Access restricted'}}
    ]
  },
  {path: '**', redirectTo: 'dashboard'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {bindToComponentInputs: false, scrollPositionRestoration: 'top'})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
