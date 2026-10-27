import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Vehicle } from '../vehicle.model';

@Component({
  selector: 'app-vehicle-detail',
  standalone: true,
  template: `<p>TODO</p>`,
})
export class VehicleDetailComponent {
  @Input() vehicle: Vehicle | null = null;
  @Output() close = new EventEmitter<void>();
}
