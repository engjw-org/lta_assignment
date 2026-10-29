import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VehicleService } from '../vehicle.service';
import { Vehicle } from '../vehicle.model';
import { SELECTED_EVENT_KEY } from '../vehicle-detail/vehicle-detail-state';

@Component({
  selector: 'app-vehicle-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './vehicle-list.component.html',
  styleUrls: ['./vehicle-list.component.css'],
})
export class VehicleListComponent implements OnInit {
  vehicles: Vehicle[] = [];
  selectedStatuses: string[] = [];
  page = 0;
  popupMessage = '';

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
    this.popupMessage = '';
    const path = `${window.location.pathname.replace(/\/$/, '')}/${encodeURIComponent(vehicle.uuid)}`;
    const popup = window.open(path, '_blank', 'popup,width=1100,height=640,resizable=yes,scrollbars=yes');
    if (!popup) {
      this.popupMessage = 'The popup was blocked. Allow popups for this site, then open the vehicle again.';
      return;
    }
    try {
      // The initial same-origin window is available before its navigation completes.
      popup.sessionStorage.setItem(SELECTED_EVENT_KEY, JSON.stringify(vehicle));
      popup.opener = null;
    } catch {
      popup.close();
      this.popupMessage = 'Unable to pass the selected event to the popup. Allow browser storage and try again.';
    }
  }
}
