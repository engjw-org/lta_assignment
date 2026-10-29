import { ApplicationConfig } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { VehicleListComponent } from './vehicle-list/vehicle-list.component';
import { VehicleDetailPageComponent } from './vehicle-detail/vehicle-detail-page.component';

export const appConfig: ApplicationConfig = {
  providers: [provideHttpClient(), provideRouter([
    { path: '', pathMatch: 'full', component: VehicleListComponent },
    { path: ':uuid', component: VehicleDetailPageComponent },
    { path: '**', redirectTo: '' },
  ])],
};
