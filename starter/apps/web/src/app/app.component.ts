import { Component } from '@angular/core';
import { VehicleListComponent } from './vehicle-list/vehicle-list.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [VehicleListComponent],
  template: `
    <h1>VRLS2 Admin</h1>
    <app-vehicle-list></app-vehicle-list>
  `,
})
export class AppComponent {}
