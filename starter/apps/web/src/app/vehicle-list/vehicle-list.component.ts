import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VehicleService } from '../vehicle.service';
import { Vehicle } from '../vehicle.model';
import { VehicleDetailComponent } from '../vehicle-detail/vehicle-detail.component';

@Component({
  selector: 'app-vehicle-list',
  standalone: true,
  imports: [CommonModule, VehicleDetailComponent],
  templateUrl: './vehicle-list.component.html',
  styleUrls: ['./vehicle-list.component.css'],
})
export class VehicleListComponent implements OnInit {
  vehicles: Vehicle[] = [];
  selectedStatuses: string[] = [];
  page = 0;
  selected: Vehicle | null = null;

  constructor(private vehicleService: VehicleService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.vehicleService.list(this.page).subscribe((rows) => {
      this.vehicles = rows;
    });
  }

  toggleStatus(status: string): void {
    const idx = this.selectedStatuses.indexOf(status);
    if (idx >= 0) {
      this.selectedStatuses.splice(idx, 1);
    } else {
      this.selectedStatuses.push(status);
    }
  }

  filteredVehicles(): Vehicle[] {
    if (this.selectedStatuses.length === 0) {
      return this.vehicles;
    }
    return this.vehicles.filter((v) =>
      this.selectedStatuses.every((s) => v.status === s),
    );
  }

  nextPage(): void {
    this.page += 1;
    this.load();
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page -= 1;
      this.load();
    }
  }

  open(vehicle: Vehicle): void {
    this.selected = vehicle;
  }

  close(): void {
    this.selected = null;
  }
}
